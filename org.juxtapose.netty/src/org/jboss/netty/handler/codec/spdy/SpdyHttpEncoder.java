/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.spdy;

import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * Encodes {@link HttpRequest}s, {@link HttpResponse}s, and {@link HttpChunk}s
 * into {@link SpdySynStreamFrame}s and {@link SpdySynReplyFrame}s.
 *
 * <h3>Request Annotations</h3>
 *
 * SPDY specific headers must be added to {@link HttpRequest}s:
 * <table border=1>
 * <tr>
 * <th>Header Name</th><th>Header Value</th>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-Stream-ID"}</td>
 * <td>The Stream-ID for this request.
 * Stream-IDs must be odd, positive integers, and must increase monotonically.</td>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-Priority"}</td>
 * <td>The priority value for this request.
 * The priority should be between 0 and 3 inclusive.
 * 0 represents the highest priority and 3 represents the lowest.
 * This header is optional and defaults to 0.</td>
 * </tr>
 * </table>
 *
 * <h3>Response Annotations</h3>
 *
 * SPDY specific headers must be added to {@link HttpResponse}s:
 * <table border=1>
 * <tr>
 * <th>Header Name</th><th>Header Value</th>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-Stream-ID"}</td>
 * <td>The Stream-ID of the request corresponding to this response.</td>
 * </tr>
 * </table>
 *
 * <h3>Pushed Resource Annotations</h3>
 *
 * SPDY specific headers must be added to pushed {@link HttpResponse}s:
 * <table border=1>
 * <tr>
 * <th>Header Name</th><th>Header Value</th>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-Stream-ID"}</td>
 * <td>The Stream-ID for this resource.
 * Stream-IDs must be even, positive integers, and must increase monotonically.</td>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-Associated-To-Stream-ID"}</td>
 * <td>The Stream-ID of the request that inititated this pushed resource.</td>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-Priority"}</td>
 * <td>The priority value for this resource.
 * The priority should be between 0 and 3 inclusive.
 * 0 represents the highest priority and 3 represents the lowest.
 * This header is optional and defaults to 0.</td>
 * </tr>
 * <tr>
 * <td>{@code "X-SPDY-URL"}</td>
 * <td>The full URL for the resource being pushed.</td>
 * </tr>
 * </table>
 *
 * <h3>Chunked Content</h3>
 *
 * This encoder associates all {@link HttpChunk}s that it receives
 * with the most recently received 'chunked' {@link HttpRequest}
 * or {@link HttpResponse}.
 *
 * <h3>Pushed Resources</h3>
 *
 * All pushed resources should be sent before sending the response
 * that corresponds to the initial request.
 */
public class SpdyHttpEncoder implements ChannelDownstreamHandler {

    private volatile int currentStreamID;

    public SpdyHttpEncoder() {
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt)
            throws Exception {
        if (!(evt instanceof MessageEvent)) {
            ctx.sendDownstream(evt);
            return;
        }

        MessageEvent e = (MessageEvent) evt;
        Object msg = e.getMessage();

        if (msg instanceof HttpRequest) {

            HttpRequest httpRequest = (HttpRequest) msg;
            SpdySynStreamFrame spdySynStreamFrame = createSynStreamFrame(httpRequest);
            int streamID = spdySynStreamFrame.getStreamID();
            ChannelFuture future = getContentFuture(ctx, e, streamID, httpRequest);
            Channels.write(ctx, future, spdySynStreamFrame, e.getRemoteAddress());

        } else if (msg instanceof HttpResponse) {

            HttpResponse httpResponse = (HttpResponse) msg;
            if (httpResponse.containsHeader(SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID)) {
                SpdySynStreamFrame spdySynStreamFrame = createSynStreamFrame(httpResponse);
                int streamID = spdySynStreamFrame.getStreamID();
                ChannelFuture future = getContentFuture(ctx, e, streamID, httpResponse);
                Channels.write(ctx, future, spdySynStreamFrame, e.getRemoteAddress());
            } else {
                SpdySynReplyFrame spdySynReplyFrame = createSynReplyFrame(httpResponse);
                int streamID = spdySynReplyFrame.getStreamID();
                ChannelFuture future = getContentFuture(ctx, e, streamID, httpResponse);
                Channels.write(ctx, future, spdySynReplyFrame, e.getRemoteAddress());
            }

        } else if (msg instanceof HttpChunk) {

            HttpChunk chunk = (HttpChunk) msg;
            SpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(currentStreamID);
            spdyDataFrame.setData(chunk.getContent());
            spdyDataFrame.setLast(chunk.isLast());

            if (chunk instanceof HttpChunkTrailer) {
                HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
                List<Map.Entry<String, String>> trailers = trailer.getHeaders();
                if (trailers.isEmpty()) {
                    Channels.write(ctx, e.getFuture(), spdyDataFrame, e.getRemoteAddress());
                } else {
                    // Create SPDY HEADERS frame out of trailers
                    SpdyHeadersFrame spdyHeadersFrame = new DefaultSpdyHeadersFrame(currentStreamID);
                    for (Map.Entry<String, String> entry: trailers) {
                        spdyHeadersFrame.addHeader(entry.getKey(), entry.getValue());
                    }

                    // Write HEADERS frame and append Data Frame
                    ChannelFuture future = Channels.future(e.getChannel());
                    future.addListener(new SpdyFrameWriter(ctx, e, spdyDataFrame));
                    Channels.write(ctx, future, spdyHeadersFrame, e.getRemoteAddress());
                }
            } else {
                Channels.write(ctx, e.getFuture(), spdyDataFrame, e.getRemoteAddress());
            }
        } else {
            // Unknown message type
            ctx.sendDownstream(evt);
        }
    }

    private ChannelFuture getContentFuture(
            ChannelHandlerContext ctx, MessageEvent e, int streamID, HttpMessage httpMessage) {
        if (httpMessage.getContent().readableBytes() == 0) {
            return e.getFuture();
        }

        // Create SPDY Data Frame out of message content
        SpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(streamID);
        spdyDataFrame.setData(httpMessage.getContent());
        spdyDataFrame.setLast(true);

        // Create new future and add listener
        ChannelFuture future = Channels.future(e.getChannel());
        future.addListener(new SpdyFrameWriter(ctx, e, spdyDataFrame));

        return future;
    }

    private class SpdyFrameWriter implements ChannelFutureListener {

        private final ChannelHandlerContext ctx;
        private final MessageEvent e;
        private final Object spdyFrame;

        SpdyFrameWriter(ChannelHandlerContext ctx, MessageEvent e, Object spdyFrame) {
            this.ctx = ctx;
            this.e = e;
            this.spdyFrame = spdyFrame;
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                Channels.write(ctx, e.getFuture(), spdyFrame, e.getRemoteAddress());
            } else if (future.isCancelled()) {
                e.getFuture().cancel();
            } else {
                e.getFuture().setFailure(future.getCause());
            }
        }
    }

    private SpdySynStreamFrame createSynStreamFrame(HttpMessage httpMessage)
            throws Exception {
        boolean chunked = httpMessage.isChunked();

        // Get the Stream-ID, Associated-To-Stream-ID, Priority, and URL from the headers
        int streamID = SpdyHttpHeaders.getStreamID(httpMessage);
        int associatedToStreamID = SpdyHttpHeaders.getAssociatedToStreamID(httpMessage);
        byte priority = SpdyHttpHeaders.getPriority(httpMessage);
        String URL = SpdyHttpHeaders.getUrl(httpMessage);
        SpdyHttpHeaders.removeStreamID(httpMessage);
        SpdyHttpHeaders.removeAssociatedToStreamID(httpMessage);
        SpdyHttpHeaders.removePriority(httpMessage);
        SpdyHttpHeaders.removeUrl(httpMessage);

        // The Connection, Keep-Alive, Proxy-Connection, and Transfer-Encoding
        // headers are not valid and MUST not be sent.
        httpMessage.removeHeader(HttpHeaders.Names.CONNECTION);
        httpMessage.removeHeader("Keep-Alive");
        httpMessage.removeHeader("Proxy-Connection");
        httpMessage.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);

        SpdySynStreamFrame spdySynStreamFrame = new DefaultSpdySynStreamFrame(streamID, associatedToStreamID, priority);
        for (Map.Entry<String, String> entry: httpMessage.getHeaders()) {
            spdySynStreamFrame.addHeader(entry.getKey(), entry.getValue());
        }

        // Unfold the first line of the message into name/value pairs
        SpdyHeaders.setVersion(spdySynStreamFrame, httpMessage.getProtocolVersion());
        if (httpMessage instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpMessage;
            SpdyHeaders.setMethod(spdySynStreamFrame, httpRequest.getMethod());
            SpdyHeaders.setUrl(spdySynStreamFrame, httpRequest.getUri());
        }
        if (httpMessage instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) httpMessage;
            SpdyHeaders.setStatus(spdySynStreamFrame, httpResponse.getStatus());
            SpdyHeaders.setUrl(spdySynStreamFrame, URL);
            spdySynStreamFrame.setUnidirectional(true);
        }

        if (chunked) {
            currentStreamID = streamID;
            spdySynStreamFrame.setLast(false);
        } else {
            spdySynStreamFrame.setLast(httpMessage.getContent().readableBytes() == 0);
        }

        return spdySynStreamFrame;
    }

    private SpdySynReplyFrame createSynReplyFrame(HttpResponse httpResponse)
            throws Exception {
        boolean chunked = httpResponse.isChunked();

        // Get the Stream-ID from the headers
        int streamID = SpdyHttpHeaders.getStreamID(httpResponse);
        SpdyHttpHeaders.removeStreamID(httpResponse);

        // The Connection, Keep-Alive, Proxy-Connection, and Transfer-ENcoding
        // headers are not valid and MUST not be sent.
        httpResponse.removeHeader(HttpHeaders.Names.CONNECTION);
        httpResponse.removeHeader("Keep-Alive");
        httpResponse.removeHeader("Proxy-Connection");
        httpResponse.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);

        SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamID);
        for (Map.Entry<String, String> entry: httpResponse.getHeaders()) {
            spdySynReplyFrame.addHeader(entry.getKey(), entry.getValue());
        }

        // Unfold the first line of the repsonse into name/value pairs
        SpdyHeaders.setStatus(spdySynReplyFrame, httpResponse.getStatus());
        SpdyHeaders.setVersion(spdySynReplyFrame, httpResponse.getProtocolVersion());

        if (chunked) {
            currentStreamID = streamID;
            spdySynReplyFrame.setLast(false);
        } else {
            spdySynReplyFrame.setLast(httpResponse.getContent().readableBytes() == 0);
        }

        return spdySynReplyFrame;
    }
}
