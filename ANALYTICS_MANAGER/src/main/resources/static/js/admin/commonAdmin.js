$(function(){
	// common-ui
	showModal();
	showNav();
	showMenu();
	uploadFile(); 
	setInputNumber();
  
	$("#adminMenuUl").find("li").each(function(){
		$(this).removeClass("customActive");
	});
	var urlName = window.location.pathname;	
	if( urlName == "/algorithmManage" )		$(".algorithmAdminActive").addClass("customActive");
	else if( urlName == "/sandboxManage" )	$(".sandboxManageAdminActive").addClass("customActive");
	else if( urlName == "/projectManage" || urlName == "/projectDetail")	$(".projectManageAdminActive").addClass("customActive");
	else if( urlName == "/batchManage" )		$(".batchManageAdminActive").addClass("customActive");
	
});


//모달 열기
var fnOpenModal = function(id){
	$("#"+id).show();
}

// 모달 닫기
var fnCloseModal = function(id){
	$("#"+id).hide();
}

