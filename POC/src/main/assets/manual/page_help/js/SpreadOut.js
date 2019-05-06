function show(e, state) {
	if (state == 1) {
		$(e).animate({
			height : "320px"
		}, 600);
		$(e).css("background-image", "url(img/box_bg_open_lager.png)");
		$(e).find(".arrow").attr("src", "img/iconfont-zuo copy.png");
		$(e).attr("onclick", "show(this,2)");
		$(e).find(".intro").fadeIn(500);

	} else {
		$(e).animate({
			height : "116px"
		}, 800, function() {
			$(e).css("background-image", "url(img/box_bg_little.png)");
			$(e).find(".arrow").attr("src", "img/iconfont-zuo copy 2.png");
			$(e).attr("onclick", "show(this,1)");
		});
		$(e).find(".intro").fadeOut(500);
	}
}

function showAll() {
	$(".box").each(function() {
		show(this, 1);
	});
}

function closeAll() {
	$(".box").each(function() {
		show(this, 0);
	});
}