$(function () {
    $("#comment").click(comment);
    $("#publishBtn").click(publish);
    $("#topBtn").click(setTop);
    $("#unTopBtn").click(setUnTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#unWonderfulBtn").click(setUnWonderful);
    $("#favoriteBtn").click(setFavorite);
    $("#unFavoriteBtn").click(setUnFavorite);
    $("#deleteBtn").click(setDelete);
});

function comment(){
    var entityType = $("#entityType").val();
    var entityId = $("#entityId").val()
    var content = editor.txt.html();
    $.post(
        CONTEXT_PATH+"/comment"+"/add/"+entityId,
        {"entityType":entityType,"entityId":entityId,"content":content},
        function () {
            location.reload();
        }
    );
}

function publish() {
    $("#publishModal").modal("hide");

    //获取标题&&内容
    var postId = $("#postId").val();
    var content = editor2.txt.html();

    //发post.ajax
    $.post(
        CONTEXT_PATH + "/discuss/update",
        {"postId":postId,"content":content},
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
                    window.location.reload();
                }
            }, 2000);
        }
    )

}

function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');

            }else{
                alert(data.msg);
            }
        }
    );
}

function setTop() {
    $.post(
        CONTEXT_PATH+"/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                location.reload();
            }else{
                alert(data.msg);
            }
        }
    );
}

function setWonderful() {
    $.post(
        CONTEXT_PATH+"/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                location.reload();
            }else{
                alert(data.msg);
            }
        }
    );
}

function setUnTop() {
    $.post(
        CONTEXT_PATH+"/discuss/unTop",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                location.reload();
            }else{
                alert(data.msg);
            }
        }
    );
}

function setUnWonderful() {
    $.post(
        CONTEXT_PATH+"/discuss/unWonderful",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                location.reload();
            }else{
                alert(data.msg);
            }
        }
    );
}

function setDelete() {
    $.post(
        CONTEXT_PATH+"/discuss/delete",
        {"id":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                location.href=CONTEXT_PATH+"/index";
            }else{
                alert(data.msg);
            }
        }
    );
}

function setFavorite() {
    $.post(
        CONTEXT_PATH+"/discuss/favorite",
        {"userId":$("#userId").val(),"entityId":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            console.log(data);
            if(data.code==0){
                location.reload();
            }else{
                alert(data.msg);
            }
        }
    );
}

function setUnFavorite() {
    $.post(
        CONTEXT_PATH+"/discuss/unFavorite",
        {"userId":$("#userId").val(),"entityId":$("#postId").val()},
        function (data) {
            data=$.parseJSON(data);
            if(data.code==0){
                location.reload();
            }else{
                alert(data.msg);
            }
        }
    );
}