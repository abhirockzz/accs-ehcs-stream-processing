/**
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
"use strict";
define(["ojs/ojcore","jquery","hammerjs","promise","ojs/ojoffcanvas"],function(a,g){a.JB={};o_("SwipeToRevealUtils",a.JB,a);a.JB.FLa=function(b,c){var d,e,f,h,k,l,m,r,t,s,q;d=g(b);d.hasClass("oj-swipetoreveal")||(d.addClass("oj-swipetoreveal"),e=d.hasClass("oj-offcanvas-start")?"end":"start",f={},f.selector=d,a.j.bka(f),h=a.j.Ks(d),null!=c&&(k=c.threshold),null!=k?(k=parseInt(k,10),/%$/.test(c.threshold)&&(k=k/100*h.outerWidth())):k=.55*h.outerWidth(),l=Math.min(.3*h.outerWidth(),d.outerWidth()),
m=!1,h.on("click.swipetoreveal",function(a){m&&(a.stopImmediatePropagation(),m=!1)}),h.on("touchstart.swipetoreveal",function(){m=!1}),d.on("ojpanstart",function(a,b){b.direction!=e?a.preventDefault():(d.children().addClass("oj-swipetoreveal-action"),s=d.children(".oj-swipetoreveal-default").get(0),t=(new Date).getTime())}).on("ojpanmove",function(a,b){m=!0;null!=s&&(b.distance>k?d.children().each(function(){this!=s&&g(this).addClass("oj-swipetoreveal-hide-when-full")}):d.children().removeClass("oj-swipetoreveal-hide-when-full"))}).on("ojpanend",
function(a,b){q=b.distance;null!=s&&q>k&&(r=g.Event("ojdefaultaction"),d.trigger(r,f),a.preventDefault());q<l&&(200<(new Date).getTime()-t||10>q)&&a.preventDefault()}))};o_("SwipeToRevealUtils.setupSwipeActions",a.JB.FLa,a);a.JB.TLa=function(b){var c;c=g(b);b={};b.selector=c;c=a.j.Ks(c);null!=c&&c.off(".swipetoreveal");a.j.qka(b)};o_("SwipeToRevealUtils.tearDownSwipeActions",a.JB.TLa,a)});