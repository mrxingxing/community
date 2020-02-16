$(function () {
    $("#topBtn").click(setTop);
    $("#unTopBtn").click(setUnTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#unWonderfulBtn").click(setUnWonderful);
    $("#deleteBtn").click(setDelete);
});


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