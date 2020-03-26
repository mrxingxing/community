$(function(){
	$("#publishBtn").click(publish);
});

function setUp(){
	$("#publishModal").modal("hide");

}

function publish() {
	$("#publishModal").modal("hide");

	//设置CSRF令牌头
/*	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e,xhr,options) {
		xhr.setRequestHeader(header,token);
	});*/

	//获取标题&&内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发post.ajax
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data = $.parseJSON(data);
			//提示框中显示返回消息
			$("hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if(data.code==0){
					//window.location.reload();
				}
			}, 2000);
		}
	)

}