function zib(a){var b,c;b=gU(a.a.ie(fIc),150);if(b==null){c=YT(pdb,Yzc,1,[gIc,hIc,iIc,jIc,kIc,lIc]);a.a.ke(fIc,c);return c}else{return b}}
function yib(a){var b,c;b=gU(a.a.ie(eIc),150);if(b==null){c=YT(pdb,Yzc,1,['bleu','rouge','jaune','vert']);a.a.ke(eIc,c);return c}else{return b}}
function eGb(a){var b,c,d,e,f,g,i;i=new Shc;Phc(i,new t2b('<b>S\xE9lectionnez votre couleur pr\xE9f\xE9r\xE9e:<\/b>'));c=yib(a.a);for(d=0;d<c.length;++d){b=c[d];e=new Kbc(dFc,b);i$b(e,'cwRadioButton-color-'+b);d==2&&(e.c.disabled=true,Ri(e,Zi(e.cb)+qFc,true));Phc(i,e)}Phc(i,new t2b('<br><b>S\xE9lectionnez votre sport pr\xE9f\xE9r\xE9:<\/b>'));g=zib(a.a);for(d=0;d<g.length;++d){f=g[d];e=new Kbc('sport',f);i$b(e,'cwRadioButton-sport-'+jqc(f,DBc,rCc));d==2&&j$b(e,(Hoc(),Hoc(),Goc));Phc(i,e)}return i}
var eIc='cwRadioButtonColors',fIc='cwRadioButtonSports';reb(815,1,LAc);_.lc=function kGb(){_gb(this.b,eGb(this.a))};yBc(wn)(1);