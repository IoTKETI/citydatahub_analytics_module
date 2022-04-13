var userRole = $("#userRole").val();
$(function(){
	
	/*템플릿 목록 선택*/
	$(document).on("click", ".templateInfo", function(){
		var choosed = $(this);
		var templateId = choosed.attr("id");
		
		if( !choosed.hasClass("activeSel") ){
			$("#templageList").find("ul li").each(function(){
				$(this).removeClass("activeSel");
			});
			$(this).addClass("activeSel");

			fnGetTemplateInfo(templateId);
		}
		$("#updateTemplate").hide();
		$("#templateInfo").fadeIn();
	});
	
	/* 커스텀 템플릿 목록 선택*/
	$(document).on("click", ".customTemplateInfo", function(){
		var choosed = $(this);
		var templateId = choosed.attr("id");
		
		if( !choosed.hasClass("activeSel") ){
			$("#customTemplateList").find("ul li").each(function(){
				$(this).removeClass("activeSel");
			});
			$(this).addClass("activeSel");
			fnGetCustomTemplateInfo(templateId);
		}
		
		if( userRole == "Analytics_Admin" ){
			$("#createTemplate").hide();
			$("#customTemplateInfo").fadeIn();
		}else{
			$("#requestCustomTemplate").hide();
			$("#customTemplateInfo").fadeIn();
		}
	});
	
	/*템플릿 신청 현황 변경*/
	$(document).on("change", "#selectState", function(){
		fnGetRequestTemplateList($(this).val());
		$("#requestCustomTemplate").hide();
		$("#customTemplateInfo").fadeIn();
	});
	
	/*템플릿 신청 버튼*/
	$(document).on("click", "#requestCustomTemplateBtn", function(){
		$("#customTemplateInfo").hide();
		$("#requestCustomTemplate").fadeIn();
		$('#requestCustomTemplate').find("input").each(function(){
			$(this).val("");
		});
		
		// 템플릿 허용 데이터 가져오기
		fnSetRequestTemplateAvailable("request");
		// 스냅샷 목록 가져오기
		fnSetTemplateSnapshot("request");
		
		// 템플릿 목록으로 탭 이동
		fnTabActive("reqListTab");
	});
	
	
	/*허가 된 데이터 사용가능 목록 클릭시*/
	$(document).on("ifClicked", "#requestCustomTemplateAvailableList input", function(){
		var id = $(this).val();
		var text = $(this).attr("data-name");
	});


	/*탬플릿 생성 버튼*/
	$(document).on("click", "#createTemplateBtn", function(){
		// 템플릿 신청 목록으로 탭 이동
		fnTabActive("reqListTab");
		
		// 모든 값 초기화
		$("#createTemplate").find("input").each(function(){
			$(this).val("");
		});
		
		$("#customTemplateInfo").hide();// 템플릿 정보 영역
		$("#createTemplate").fadeIn(); // 템플릿 생성 영역
		$("#copyHdfsDataCommand").text("");
		$("#copyHiveDataCommand").text("");
		
		// 템플릿 허용 데이터 가져오기
		fnSetRequestTemplateAvailable("create");
		// 스냅샷 목록 가져오기
		fnSetTemplateSnapshot("create");
	});
	
	/* 신청 템플릿 생성 버튼*/
	$(document).on("click", "#addTemplateBtn", function(){
		// 모든 값 초기화
		$("#createTemplate").find("input").each(function(){
			$(this).val("");
		});
		
		$("#customTemplateInfo").hide();
		$("#createTemplate").fadeIn();
		$("#copyHdfsDataCommand").text("");
		$("#copyHiveDataCommand").text("");
		
		// 신청 템플릿 생성 데이터 입력
		fnSetCreateTemplateData();
		// 스냅샷 목록 가져오기
		fnSetTemplateSnapshot("create");
		drawCommands("create");
	});
	
	/*템플릿 수정 버튼*/
	$(document).on("click", "#updateTemplateBtn", function(){
		// 모든 값 초기화
		$("#updateTemplate").find("input").each(function(){
			$(this).val("");
		});
		
		$("#templateInfo").hide();
		$("#updateTemplate").fadeIn();
		// 템플릿 수정 데이터 입력
		fnSetUpdateTemplate();
		drawCommands("update");
	});
	
	/*템플릿 삭제 버튼*/
	$(document).on("click", "#delteTemplateBtn", function(){
		fnDeleteTemplate();
	});
	
	/*모든 사용자 체크*/
	$(document).on("click", "#publicFlag", function(){
		if( $(this).is(":checked") )	$("#inputUuserId").fadeOut();
		else							$("#inputUuserId").fadeIn();
	});
	
	/*모든 사용자 체크*/
	$(document).on("click", "#u_publicFlag", function(){
		if( $(this).is(":checked") )	$("#u_inputUuserId").fadeOut();
		else							$("#u_inputUuserId").fadeIn();
	});
	
	// 템플릿 추가시 
	$(document).on("change", "#templateAvailableList input", function(){
		drawCommands("create");
	});
	$("#startDate").change(function(){
		drawCommands("create");
	});
	$("#endDate").change(function(){
		drawCommands("create");
	});

	// 템플릿 수정시
	$(document).on("change", "#u_templateAvailableList input", function(){
		drawCommands("update");
	});
	$("#u_startDate").change(function(){
		drawCommands("update");
	});
	$("#u_endDate").change(function(){
		drawCommands("update");
	});

})


/*템플릿 관리 */
var fnTemplateManage = function(){
	/*템플릿 목록 가져오기*/
	fnGetTemplateList();
	
	/*템플릿 신청목록 가져오기*/
	fnGetRequestTemplateList("ongoing");
	
	fnTabActive("listTab"); // 템플릿 목록으로 탭 이동
	fnOpenModal("templateAdmin_modal");
}

/*템플릿 목록 가져오기*/
var fnGetTemplateList = function(option){
	var response = fnGetTemplateListByAjax();
	if( option == "createInstance" ){
		fnCreateInstanceTemplateHtml(response.templates);
		
	}else{
		var templates = response.templates;
		var html = "";
		if( templates.length > 0 ){
			html += "<ul class='select_list'>";
			for( var i in templates ){
				var data = templates[i];
				if( i == 0 ){
					html += "<li class='templateInfo activeSel' style='cursor:pointer'" +
							"id="+data.ANALYSIS_TEMPLATE_SEQUENCE_PK+">" +
									"<a role='button'>"+data.NAME+"</a></li>";
					/*템플릿 정보 생성*/
					fnCreateTemplateInfo(data); 
				}else{
					html += "<li class='templateInfo' style='cursor:pointer'" +
							"id="+data.ANALYSIS_TEMPLATE_SEQUENCE_PK+">" +
									"<a role='button'>"+data.NAME+"</a></li>";
				}
			}
			html += "</ul>";
			$("#templageList").html(html);
			$(".templageModifyBtn").show();
		}else{
			$("#templageList").text("템플릿 목록이 없습니다.");
			$(".templageModifyBtn").hide();
		}
	}
}

/*템플릿 정보 가져오기*/
var fnGetTemplateInfo = function(templateId, option){
	console.log("fnGetTemplateInfo: templateId: "+templateId+", option: "+option);
	var template = fnGetTemplateByAjax(templateId);
	if(option == "createInstance" )	fnCreateInstanceTemplateInfo(template);
	else fnCreateTemplateInfo(template);
}


/*템플릿 정보 생성*/
var fnCreateTemplateInfo = function(data){
	console.log(data);
	var templateDataSummaryHtml = "";
	var instanceDataSummaryHtml = "";
	var templatePeriod = " - "+data.DATA_STARTDATE+" ~ "+data.DATA_ENDDATE;
	
	var templateDataSummary = data.DATA_SUMMARY.availableList;
	for( var i in templateDataSummary ){
		var num = Number(1)+Number(i);
		templateDataSummaryHtml += "<li>&nbsp;&nbsp;"+num+". "+templateDataSummary[i].name+"</li>";
	}

	if( data.PUBLIC_FLAG == true || data.PUBLIC_FLAG == "true" ){
		$("#useUserId").text(" - 모든 사용자");
	}else{
		$("#useUserId").text(" - "+data.userId);
	}
	
	$("#templateDataSummary").html(templateDataSummaryHtml);
	$("#templatePeriod").text(templatePeriod);
}


/*템플릿 신청목록 가져오기  */
var fnGetRequestTemplateList = function(state){
	var customTemplates = fnGetRequestTemplateListByAjax();
	var html = "";
	var cnt = 0;
	if( fnNotNullAndEmpty(customTemplates) ){
		$("#selectState").val(state);
		if( customTemplates.length > 0 ){
			for( var i in customTemplates ){
				var data = customTemplates[i];
				if( data.PROGRESS_STATE == state ){
					if( html == "" ){
						html += "<ul class='select_list'><li class='customTemplateInfo activeSel' style='cursor:pointer'" +
								"id="+data.CUSTOM_ANALYSIS_TEMPLATE_REQUEST_SEQUENCE_PK+">" +
										"<a role='button'>"+data.NAME+"</a></li>";
						/*템플릿 정보 생성*/
						fnSetCustomTemplate(data);
					}else{
						html += "<li class='customTemplateInfo' style='cursor:pointer'" +
								"id="+data.CUSTOM_ANALYSIS_TEMPLATE_REQUEST_SEQUENCE_PK+">" +
										"<a role='button'>"+data.NAME+"</a></li>";
					}
					cnt++;
				}
			}
			
			if( cnt > 0 ){
				html += "</ul>";
				$("#customTemplateList").html(html);
				$(".addTemplateBtn").show();
				
			}else{
				$("#customTemplateList").text("템플릿 목록이 없습니다.");
				$(".addTemplateBtn").hide();
				$("#customTemplateInfoContent").hide();
			}
			$("#requestCustomTemplate").hide();
			$("#customTemplateInfo").fadeIn();
			
		}else{
			$("#customTemplateList").text("템플릿 목록이 없습니다.");
			$(".addTemplateBtn").hide();
			$("#customTemplateInfoContent").hide();
		}
	}else{
//		fnComErrorMessage("템플릿 신청목록 가져오기 에러");
	}
}

/* 커스텀 템플릿 정보 가져오기*/
var fnGetCustomTemplateInfo = function(templateId){
	var customTemplateRequest = fnGetCustomTemplateByAjax(templateId);
	fnSetCustomTemplate(customTemplateRequest);
}


/* 커스텀 템플릿 정보 생성*/
var fnSetCustomTemplate = function(data){
	var customTemplateDataSummaryHtml = "";
	var customTemplatePeriod = " - "+data.DATA_STARTDATE+" ~ "+data.DATA_ENDDATE;
	
	var customTemplateDataSummary = data.DATA_SUMMARY.availableList;
	for( var i in customTemplateDataSummary ){
		var num = Number(1)+Number(i);
		customTemplateDataSummaryHtml += "<li>&nbsp;&nbsp;"+num+". "+customTemplateDataSummary[i].name+"</li>";
		
	}

	$("#customTemplateDataSummary").html(customTemplateDataSummaryHtml);
	$("#customTemplatePeriod").text(customTemplatePeriod);
	$("#createDate").text("1. 신청일 : "+data.createDataTime);
	$(".createDate").text(data.createDataTime);
	$("#progressState").text("2. 상태 : "+convertProgressState(data.PROGRESS_STATE));
	if( fnNotNullAndEmpty(data.ADMIN_COMMENT) ){
		$("#adminComment").text("3. 기타 : "+data.ADMIN_COMMENT);
		$(".adminComment").val(data.ADMIN_COMMENT);
	}else{
		$("#adminComment").text("기타 : - ");
		$(".adminComment").val("");
	}
	$("#customTemplateInfoContent").show();
}

/*진행상태 변환*/
var convertProgressState = function(type){
	switch(type){
    case 'standby':
        return '대기';
    case 'reject':
        return '거절';
    case 'ongoing':
        return '생성준비중';
    default:
        return '';
	}
}

/*커스텀 템플릿 상태 변경*/
var fnChangeCustomTemplate = function(option){
	var templateId = "";
	var comment = "템플릿 신청을 취소하겠습니까?";
	var state = "cancel";
	var data = {};

	if( option != "cancel" ){
		comment = "변경하시겠습니까?";
		data["adminComment"] = $(".adminComment").val();
		data["progressState"] = $("#changeState").val();
	}else{
		data["progressState"] = "cancel";
	}
	
	console.log(data);
	if( confirm(comment) ){
		// 선택된 템플릿 ID 가져오기
		$("#customTemplateList").find("ul li").each(function(){
			if( $(this).hasClass("activeSel") ){
				templateId = $(this).attr("id");
			}
		});
		var response = fnChangeCustomTemplateByAjax(templateId, data);
		if( response.result == "success" ){
			fnGetRequestTemplateList($("#selectState").val());
			
			if( option == "cancel" ){
				fnComNotify("success","템플릿 신청을 취소하였습니다.");
			}else{
				fnComNotify("success","템플릿 상태를 변경하였습니다.");
			}
			
		}else{
			if( option == "cancel" ) fnComErrorMessage("신청취소 에러!!", response.detail);
			else 		fnComErrorMessage("상태변경 에러!!", response.detail);
		}
	}
}

/*템플릿 허용 데이터 가져오기*/
var fnSetRequestTemplateAvailable = function(option){
	var availableList = fnGetRequestTemplateAvailableByAjax();
	var html = "";
	html = fnCreateAvailableHtml(availableList, option);
	if( option == "request" )	$("#requestCustomTemplateAvailableList").html(html); // 사용자 템플릿 요청시
	else if( option == "update" )	$("#u_templateAvailableList").html(html);
	else $("#templateAvailableList").html(html);
}

/*스냅샷 목록 가져오기*/
var fnSetTemplateSnapshot = function(option){
	var html = fnCreateSnapshotHtml(fnSetTemplateSnapshotByAjax(), option);
	if( option == "create" )	$("#templateSnapshotId").html(html);
	else if( option == "update" )	$("#u_templateSnapshotId").html(html);
}

/*템플릿 허용 목록 생성*/
var fnCreateAvailableHtml = function(availableList, option){
	var html = "";
	if( availableList.length > 0 ){
		if( option == "request" ){
			html += "<label class='control-label labelTitle'>허가 된 데이터 사용가능 목록</label>";			
		}else{
			html += "<label class='control-label labelTitle'>이미지에 저장되어있는 데이터 정보</label>";
		}
	}
	for( var i in availableList ){
		var data = availableList[i];

		if( option == "update" ){
			html += "<div class='cheakarea checkboxCustom'><input type='checkbox' id='u_check_"+data.id+"' value='"+data.id+"' data-name='"+(data.name==""?data.id:data.name)+"'><label for='u_check_"+data.id+"'> "+(data.name==""?data.id:data.name)+"</label></div>";
		}else{
			html += "<div class='cheakarea checkboxCustom'><input type='checkbox' id='check_"+data.id+"' value='"+data.id+"' data-name='"+(data.name==""?data.id:data.name)+"'><label for='check_"+data.id+"'> "+(data.name==""?data.id:data.name)+"</label></div>";
		}
		
	}
	if( availableList.length > 0 ) html += "</div>";
	return html;
}

/*스냅샷 목록 생성*/
var fnCreateSnapshotHtml = function(snapshotList){
	var html = "";
	if( snapshotList.length > 0 ){
		for( var i in snapshotList ){
			var data = snapshotList[i];
			html += "<option value='"+data.id+"'>"+data.name+"</option>";
		}
	}
	return html;
}


/*템플릿 허용 데이터 생성*/
var fnCreateAvailableDataHtml = function(id, text, availableDataList){
	var html = "";
	if( availableDataList.length > 0 )
		html += "<div class='col-md-4 col-sm-6 col-xs-6 item' id='"+id+"'><label class='control-labe labelTitle'>"+text+"</label>";
		
	for( var i in availableDataList ){
		var data = availableDataList[i];
		html += "<div><label><input type='checkbox' class='flat' value='"+data.id+"'> "+data.name+"</label></div>";
	}
	if( availableDataList.length > 0 ) html += "</div>";
	return html;
}

/*샌드박스 템플릿 추가 요청*/
var fnRequestCustomTemplate = function(){
	// validation 체크
	if( $.trim($("#requestCustomTemplateName").val()) == "" ){
		fnComNotify("warning", "템플릿명을 입력해주세요.");
		$("#requestCustomTemplateName").focus();
		return false;
		
	}else if( $.trim($("#requestCustomStartDate").val()) == "" ){
		fnComNotify("warning", "시작일을 선택해주세요.");
		$("#requestCustomStartDate").focus();
		return false;
		
	}else if( $.trim($("#requestCustomEndDate").val()) == "" ){
		fnComNotify("warning", "종료일을 선택해주세요.");
		$("#requestCustomEndDate").focus();
		return false;
		
	}else{
		var data = {}, dataSummary = {};
		
		var list = new Array();
		$("#requestCustomTemplateAvailableList").find("input").each(function(){
			if( $(this).is(":checked") ){
				var data = {};
				data["id"] = $.trim($(this).val());
				data["name"] = $.trim($(this).attr("data-name"));
				list.push(data);
			}
		});

		dataSummary["availableList"] = list;
		
		data["name"] = $("#requestCustomTemplateName").val();
		data["dataSummary"] = dataSummary;
		data["dataStartDate"] = $("#requestCustomStartDate").val();
		data["dataEndDate"] = $("#requestCustomEndDate").val();
		
		if( dataSummary.availableList.length == 0 ){
			fnComNotify("warning", "허가 된 데이터 사용가능 목록을 선택해주세요.");
			return false;
			
		}else{
			if( confirm("템플릿을 신청 하시겠습니까?") ){
				$("#loading").show();
				var response = fnRequestCustomTemplateByAjax(data);
				if( response == "success" ){
					$("#stateText").text("생성준비중  ");
					fnGetRequestTemplateList("ongoing");
					fnComNotify("success", "템플릿 추가 요청을 하였습니다.");
					$("#requestCustomTemplate").hide();
					$("#customTemplateInfo").fadeIn();
					$("#requestCustomTemplateName").val("");
					$("#requestCustomStartDate").val("");
					$("#requestCustomEndDate").val("");
					$("#loading").hide();
				}else{
					$("#loading").hide();
				}
			}
		}
	}
}

/*신청 템플릿 생성 데이터 입력*/
var fnSetCreateTemplateData = function(){
	// 템플릿 허용 데이터 가져오기
	fnSetRequestTemplateAvailable("create");
	
	var templateId = "";
	// 선택된 템플릿 ID 가져오기
	$("#customTemplateList").find("ul li").each(function(){
		if( $(this).hasClass("activeSel") ){
			templateId = $(this).attr("id");
		}
	});
	$("#customTemplateId").val(templateId);
	
	var data = fnGetCustomTemplateByAjax(templateId);
	var availableList = data.DATA_SUMMARY.availableList;
	
	$("#templateName").val(data.NAME);
	$("#startDate").val(data.DATA_STARTDATE);
	$("#endDate").val(data.DATA_ENDDATE);
	if( data.PUBLIC_FLAG == true || data.PUBLIC_FLAG == "true" ){
		$("#publicFlag").prop("checked",true);
		$("#inputUuserId").hide();
	}else{
		$('#templateUserId').val(data.USER_ID);
		$("#inputUuserId").show();
	}
	
		
	for( var i in availableList ){
		var availableData = availableList[i];
		$("#templateAvailableList").find("input").each(function(){
			if( $(this).val() == availableData.id )
				$(this).prop("checked",true);
		});
		
	}
}

/*템플릿 등록/수정 Validation*/
var fnValidationTemplateData = function(type){
	var id = "";
	if( type == "update" ) id="u_";

	// validation 체크
	if( $.trim($("#"+id+"templateName").val()) == "" ){
		fnComNotify("warning", "템플릿명을 입력해주세요.");
		$("#"+id+"templateName").focus();
		return false;
		
	}else if( $.trim($("#"+id+"startDate").val()) == "" ){
		fnComNotify("warning", "시작일을 선택해주세요.");
		$("#"+id+"startDate").focus();
		return false;
		
	}else if( $.trim($("#"+id+"endDate").val()) == "" ){
		fnComNotify("warning", "종료일을 선택해주세요.");
		$("#"+id+"endDate").focus();
		return false;
		
	}else if( !$("#"+id+"publicFlag").is(":checked") && $("#"+id+"templateUserId").val() == "" ){
		fnComNotify("warning", "사용자 아이디를 입력해주세요.");
		$("#"+id+"templateUserId").focus();
		return false;
		
	}else{

		var data = {}, dataSummary = {};
		var list = new Array();
		$("#"+id+"templateAvailableList").find("input").each(function(){
			if( $(this).is(":checked") ){
				var data = {};
				data["id"] = $.trim($(this).val());
				data["name"] = $.trim($(this).attr("data-name"));
				list.push(data);
			}
		});
		
		dataSummary["availableList"] = list;
		
		data["customTemplateId"] = $("#"+id+"customTemplateId").val();
		data["name"] = $("#"+id+"templateName").val();
		data["dataSummary"] = dataSummary;
		data["snapshotId"] = $("#"+id+"templateSnapshotId").val();
		data["dataStartDate"] = $("#"+id+"startDate").val();
		data["dataEndDate"] = $("#"+id+"endDate").val();
		data["publicFlag"] = $("#"+id+"publicFlag").is(":checked") ? true : false;
		data["userIdList"] = $("#"+id+"templateUserId").val();
		
		if( dataSummary.availableList.length == 0 ){
			fnComNotify("warning", "이미지에 저장되어있는 데이터를 선택해주세요.");
			return false;
		}
	}
	return data;
}

/*템플릿 생성*/
var fnCreateTemplate = function(){
	var data = fnValidationTemplateData("create"); // 템플릿 등록/수정 Validation
	if( data != "" ){
		
		if( confirm("템플릿을 생성하시겠습니까?") ){
			var response = fnCreateTemplateByAjax(data);
			if( response.result == "success" ){
				$('a[href="#tab_content1"]').click();

				/*템플릿 목록 가져오기*/
				fnGetTemplateList();
				
				/*템플릿 신청목록 가져오기*/
				fnGetRequestTemplateList("ongoing");
				
				$("#createTemplate").hide();
				$("#customTemplateInfo").fadeIn();
				fnComNotify("success", "템플릿을 생성하였습니다.");
				
			}else if( response.detail == "duplicateName"){
				fnComNotify("warning","템플릿명이 중복되었습니다.");
				
			}else{
				fnComErrorMessage("템플릿 생성 에러!!", response.detail);
			}
		}
	}
}

/*템플릿 삭제*/
var fnDeleteTemplate = function(){
	var templateId = "", templateName = "";
	$("#templageList").find("li").each(function(){
		if( $(this).hasClass("activeSel") ){
			templateId = $(this).attr("id");
			templateName = $(this).text();
		}
	});
	
	if( confirm(templateName+"을 삭제하시겠습니까?") ){
		var response = fnDeleteTemplateByAjax(templateId);
		if( response.result == "success" ){
			fnComNotify("success", templateName+"을 삭제하였습니다.");
			fnGetTemplateList();
			
		}else{
			fnComErrorMessage("템플릿 삭제 에러!!", response.detail);
		}
	}
}

/*템플릿 수정 데이터 입력*/
var fnSetUpdateTemplate = function(){
	var templateId = "", templateName = "";
	$("#templageList").find("li").each(function(){
		if( $(this).hasClass("activeSel") ){
			templateId = $(this).attr("id");
			templateName = $(this).text();
		}
	});
	
	$("#templateId").val(templateId);
	// 스냅샷 목록 가져오기
	fnSetTemplateSnapshot("update");
	// 데이터 사용목록 가져오기
	fnSetRequestTemplateAvailable("update");

	var template = fnGetTemplateByAjax(templateId);
	var availableList = template.DATA_SUMMARY.availableList;
	console.log(template);
	$("#u_templateName").val(template.NAME);
	$("#u_templateSnapshotId").val(template.SNAPSHOT_ID);
	$("#u_startDate").val(template.DATA_STARTDATE);
	$("#u_endDate").val(template.DATA_ENDDATE);
	
	if( template.PUBLIC_FLAG == true || template.PUBLIC_FLAG == "true" ){
		$("#u_publicFlag").prop("checked", true);
		$('#u_templateUserId').val("");
		$("#u_inputUuserId").hide();
	}else{
		$("#u_publicFlag").prop("checked", false);
		$('#u_templateUserId').val(template.userId);
		$("#u_inputUuserId").show();
	}		
	
	for( var i in availableList ){
		var availableData = availableList[i];
		$("#u_templateAvailableList").find("input").each(function(){
			if( $(this).val() == availableData.id )
				$(this).prop("checked", true);
		});
	}
}

/*템플릿 수정*/
var fnUpdateTemplate = function(){
	var templateId = $("#templateId").val();
	var data = fnValidationTemplateData("update"); // 템플릿 등록/수정 Validation
	
	if( confirm("수정하시겠습니까?") ){
		var response = fnUpdateTemplateByAjax(templateId, data);
		if( response.result == "success" ){
			fnGetTemplateList();
			$("#requestCustomTemplate").hide();
			$("#createTemplate").hide();
			$("#updateTemplate").hide();
			$("#templateInfo").fadeIn();
			fnComNotify("success", "템플릿을 수정하였습니다.");
			
		}else if( response.detail == "duplicateName"){
			fnComNotify("warning","템플릿명이 중복되었습니다.");
			
		}else{
			fnComErrorMessage("템플릿 수정 에러!!", response.detail);
		}
	}
}

var fnTabActive = function(tab){
	if( tab == "reqListTab" ){
		// 템플릿 신청 목록으로 탭 이동
		$(".listTab").removeClass("on");
		$(".listTabView").removeClass("tabViewOn");
		
		$(".reqListTab").addClass("on");
		$(".reqListTabView").addClass("tabViewOn");		
		
	}else{
		// 템플릿 목록으로 탭 이동
		$(".reqListTab").removeClass("on");
		$(".reqListTabView").removeClass("tabViewOn");
		
		$(".listTab").addClass("on");
		$(".listTabView").addClass("tabViewOn");
	}
}

var drawCommands =function (option){
	var commands = fnMakeCommand(option);
	var checkId = "";
	if( option == "update") checkId = "u_";	
	$("#"+checkId+"copyHdfsDataCommand").text(commands.copyHdfsDataCommand);
	$("#"+checkId+"copyHiveDataCommand").text(commands.copyHiveDataCommand);		
};

var fnMakeCommand = function(option){
	// validation 체크
	var checkId = "";
	if( option == "update") checkId = "u_";
	 if( $.trim($("#"+checkId+"startDate").val()) == "" ){
		return false;
	}else if( $.trim($("#"+checkId+"endDate").val()) == "" ){
	 	return false;
	}else{
	 
		var list = new Array();
		var startDate=$("#"+checkId+"startDate").val().replace(/-/gi,"").substr(0,6);
		var endDate=$("#"+checkId+"endDate").val().replace(/-/gi,"").substr(0,6);
 
		$("#"+checkId+"templateAvailableList").find("input").each(function(){
			if( $(this).is(":checked") ){
				var id= $.trim($(this).val());
				list.push(id);
			}
		});

		var domainsAsString="";

		for(i=0;i<list.length;i++){
			if(i==0){
				domainsAsString+=list[i];
			}else{
				domainsAsString+=(","+list[i]);
			}
		}

		var copyCommands={};
		copyCommands["copyHdfsDataCommand"]="/home/centos/SANDBOX_DATA_TOOL/copyHdfsData.sh -d "+domainsAsString+" -s "+startDate+" -e "+endDate+" -u "+ "hdfs://IP:port/encore/etl/Trust";
		copyCommands["copyHiveDataCommand"]="/home/centos/SANDBOX_DATA_TOOL/copyHiveDDL.sh -d "+domainsAsString+" -u "+"jdbc:hive2://IP:port/smartcity";
  		return copyCommands;
	}
};