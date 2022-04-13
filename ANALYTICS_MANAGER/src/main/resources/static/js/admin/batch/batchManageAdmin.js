var clickRow, batchServiceRequestSequencePk, batchServiceSequencePk;
var userRole = $("#userRole").val();
$("#loading").show();

$(function(){
	fnInit();
	
	/* 10초 주기로 반복 조회 후 상태값 변경 */
	setInterval(function(){
		fnChangeBatchServerState();/*배치서버 상태값 갱신*/
		fnChangeBatchState();/*배치실행상태 체크*/
	},30000);
	
	// 배치등록 버튼 클릭시
	$(document).on("click", "#addBatchModalBtn", function(){
		$("#batchForm").find("input").each(function(){
			$(this).val("");
		});
		$("#userRequestTerm").val("");
		$(".modalName").text("등록");

		$("#makeDataMethod").val("nifi"); //  데이터 생성방식
		$("#sql").val(""); // 데이터 생성 방식 중 SQL 선택 시 SQL 구문
		$("#targetType").val("rawData"); // 후처리 시 타깃 처리방식
		$("#datasetId").val(""); // 코어모듈 저장 시 데이터셋 아이디
		$("#totalColumnName").val(""); // 전체값을 포함하는 컬럼이름
		// 배치서버는 하나만 사용하므로, Batch server Sequence Instance PK인 '1'로 고정
		$("#batchInstanceSequenceFk2").val("1"); 

		$("#batchNifiButton").show();
		// 등록할 배치서버 가져오기
		fnGetBatchServerList();
		// if( $("#batchInstanceSequenceFk2").val() == null ){
		// 	fnComNotify("warning", "배치서버를 생성해주세요.");
		// 	return false;
		// }
		
		// 사용자별 프로젝트, 모델 목록 가져오기
		fnGetProjectOfUserId();
		if( $("#selectedProject li").length == 0 ){
			// fnComNotify("warning", "샌드박스를 생성해주세요.");
			return false;
		}
		
		if( $("#selectedModel li").length == 0 ){
			fnComNotify("warning", "프로젝트에서 모델을 생성해주세요.");
			return false;
		}

		$("#updateAttribute li.active").each(function (){$(this).removeClass("active");});

		$("#updateAttribute li[data-attribute='inputData']").each(function(){$(this).addClass("active")});
		$("#updateAttribute li[data-attribute='predictValue']").each(function(){$(this).addClass("active")});
		$("#updateAttribute li[data-attribute='observedAt']").each(function(){$(this).addClass("active")});

		// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
		fnGetRequestTemplateAvailable();

		showMakeDataMethod();
		showStoreMethod();

		$(".rejectBtn").hide();
		$(".registDiv").show();

		fnOpenModal("batchModal");
	});
	
	
	/* 배치목록 클릭시 */
	$(document).on("click", "#batchTbodyHtml td", function(){
		if( $(this).index() == 1 ){
			var clickRows = $("#logTable_batchList").dataTable().fnGetPosition(this); // 변경하고자 하는 clickRow
			clickRow = clickRows[0];
			var data = $("#logTable_batchList").dataTable().fnGetData($(this).parent());
			batchServiceSequencePk = data[1];
			
			$(".modalName").text("수정");
			$("#batchNifiButton").hide();
			
			// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
			fnGetRequestTemplateAvailable();
			var batch = fnGetBatchServiceByAjax(batchServiceSequencePk);
			
			var executionCycleArr = batch.EXECUTION_CYCLE.split(" ");
			for( var i in executionCycleArr ){
				$("#executionCycle_"+i).val(executionCycleArr[i]); // 실행주기
			}
			
			$("#name").val(batch.NAME); // 배치명
			$("#nifiTemplateName").val(batch.NIFI_TEMPLATE_NAME); // NIFI 템를릿명
			$("#applyDataPath").val(batch.APPLY_DATA_PATH); // 파일 생성 위치
			$("#applyDataNameRule").val(batch.APPLY_DATA_NAME_RULE); // 파일 생성 규칙			
			$("#resultUpdateDomain").val(batch.RESULT_UPDATE_DOMAIN_ID); // 도메인명
			$("#executionCycle").val(batch.EXECUTION_CYCLE); // 실행주기
			$("#resultUpdateMethod").val(batch.RESULT_UPDATE_METHOD); // 결과만영방식
			$("#enrollmentTerm").val(batch.ENROLLMENT_TERM); // 요청사항
			$("#storeMethod").val(batch.STORE_METHOD); // 저장방법
			$("#totalColumnName").val(batch.TOTAL_COLUMN_NAME); // 전체값을 포함하는 컬럼이름
			$("#domainIdColumnName").val(batch.DOMAIN_ID_COLUMN_NAME); // 도메인컬럼이름


			$("#updateAttribute li.active").each(function (){$(this).removeClass("active");});

			if(batch.UPDATE_ATTRIBUTE!=null){
				res=batch.UPDATE_ATTRIBUTE.split(",");

				for(i=0;i<res.length;i++){
					$("#updateAttribute li[data-attribute='"+res[i]+"']").each(function(){$(this).addClass("active")});
				}
			}


			$("#makeDataMethod").val(batch.MAKE_DATA_METHOD); //  데이터 생성방식
			$("#sql").val(batch.SQL); // 데이터 생성 방식 중 SQL 선택 시 SQL 구문
			$("#targetType").val(batch.TARGET_TYPE); // 후처리 시 타깃 처리방식
			$("#datasetId").val(batch.DATASET_ID); // 코어모듈 저장 시 데이터셋 아이디

			
			$(".rejectBtn").hide();
			$(".registDiv").hide();

			showMakeDataMethod();
			showStoreMethod();

			fnOpenModal("batchModal");
		}
	});
	
	/* 배치신청 목록 클릭시 */
	$(document).on("click", "#batchRequestTbodyHtml td", function(){
		if( $(this).index() == 11 ){
			var clickRows = $("#logTable_batchRequestList").dataTable().fnGetPosition(this); // 변경하고자 하는 clickRow
			clickRow = clickRows[0];
		}
	});
	
	// 프로젝트  클릭시
	$(document).on("click", ".projectList", function(){
		$(".projectList").removeClass("active");
		$(this).addClass("active");
		fnGetModelsOfProjectPk($(this).attr("data-projectSequencePk"), "useOfBatch");
	});

	// 프로젝트  클릭시
	$(document).on("click", ".updateAttributeList", function(){
		if($(this).hasClass("active")){
			$(this).removeClass("active");
		}else{
			$(this).addClass("active");
		}
	});

	// 모델  클릭시
	$(document).on("click", ".modelList", function(){
		$(".modelList").removeClass("active");
		$(this).addClass("active");
	});

	$(document).on("change", "#makeDataMethod", function(){

		showMakeDataMethod();
	});

	$(document).on("change", "#storeMethod", function(){

		showStoreMethod();
	});

});

var showStoreMethod= function(){
	if($("#storeMethod").val()=="rawData"){
		$("#totalColumnName").parent().parent().hide();
		$("#targetType").parent().parent().hide();


		$("#storeMethod option .select").removeClass("select");
		$("#storeMethod option[value=rawData]").addClass("select");

	}else if($("#storeMethod").val()=="percentage"){
		$("#targetType").parent().parent().hide();
		$("#totalColumnName").parent().parent().show();

		$("#storeMethod option").removeClass("select");
		$("#storeMethod option[value=percentage]").addClass("select");

	} else{
		console.log("조건에 없는 것을 선택함.")
	}
};

var showMakeDataMethod= function(){
	if($("#makeDataMethod").val()=="nifi"){
		$("#sql").parent().parent().hide();
		$("#nifiTemplateName").parent().parent().show();

		$("#makeDataMethod option .select").removeClass("select");
		$("#makeDataMethod option[value=nifi]").addClass("select");

	}else if($("#makeDataMethod").val()=="sql"){
		$("#nifiTemplateName").parent().parent().hide();
		$("#sql").parent().parent().show();

		$("#makeDataMethod option").removeClass("select");
		$("#makeDataMethod option[value=sql]").addClass("select");

	}else{
		console.log("조건에 없는 것을 선택함.")
	}
};

/*테이블 생성*/
var createTable = function(){
	  /*배치 목록*/
	  $("#logTable_batchList").DataTable( {
		  	"language" : language
			,"autoWidth": false
	  } );
	  $('#logTable_batchList').DataTable().columns([1]).visible(false);
	
  
	  /*배치 신청 목록*/
	  $("#logTable_batchRequestList").DataTable( {
		  	"language" : language		  
			,"autoWidth": false
	  } );
	  $('#logTable_batchRequestList').DataTable().columns([1]).visible(false);
	
	  /*배치 서버 목록*/
	  $("#logTable").DataTable( {
		  	"language" : language		  
			,"autoWidth": false
	  } );
	
	  /*배치 이력 목록*/
	  fnSearchBatchLog();
	  
	  $('.dataTables_filter').hide();
	  $('.dataTables_filter_custom').hide();
}


var fnInit = function(){
	$(".breadcrumb__list--current").text("배치 관리");
	fnSetDatepicker("startDate","endDate");
	fnSearch();
}

/*배치 목록 조회*/
var fnSearch = function(){
	// 배치 목록 조회
	var batchList = fnGetBatchServicesByAjax();
	$("#batchTbodyHtml").html(fnCreateBatchListHtml(batchList));
	
	// 배치 신청 목록 조회
	// var batchRequestList = fnGetbatchServiceRequestsByAjax();
	// $("#batchRequestTbodyHtml").html(fnCreateBatchRequestListHtml(batchRequestList));
	
	// 배치 서버 목록 조회
	// var batchServerList = fnGetBatchServerListByAjax();
	// $("#tbodyHtml").html(fnCreateBatchServerListHtml(batchServerList));
	
	// 배치 이력 목록 조회
	var batchLogList = fnGetBatchLogListByAjax($("#startDate").val(), $("#endDate").val());
	$("#batchLogTbodyHtml").html(fnCreateBatchLogListHtml(batchLogList));
	
	$("#loading").hide();
}

/*배치 목록 생성*/
var fnCreateBatchListHtml = function(list){
	var html = "";
	for( var i in list ){
		var data = list[i];
		html += "<tr>";
		html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_SEQUENCE_PK+"'></label></div></td>";
		html += "	<td>"+data.BATCH_SERVICE_SEQUENCE_PK+"</td>";
		html += "	<td><a class='js-modal-show' href='#batchModal' title="+data.NAME+">"+data.NAME+"</a></td>";
		html += "	<td title='"+data.projectName+"'>"+data.projectName+"</td>";
		html += "	<td title="+data.modelName+">"+data.modelName+"</td>";
		// html += "	<td title='"+data.batchServer+"'>"+data.batchServer+"</td>";
		html += "	<td title="+data.MAKE_DATA_METHOD+">"+data.MAKE_DATA_METHOD+"</td>";
		html += "	<td title="+data.RESULT_UPDATE_DOMAIN_NAME+">"+data.RESULT_UPDATE_DOMAIN_NAME+"</td>";
		if( data.RESULT_UPDATE_METHOD == "replace" )	html += "	<td title=REPLACE>REPLACE</td>";
		else 	html += "	<td title=UPDATE>UPDATE</td>";
		html += "	<td>"+data.EXECUTION_CYCLE+"</td>";
		
		// if( data.USE_FLAG == "true" )		html += "	<td>작동중</td>";
		// else 								html += "	<td>정지</td>";
		
		if( data.BATCH_STATE == "success" )		html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>성공<div></td>";
		else if( data.BATCH_STATE == "stop" && data.USE_FLAG == "true")		html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>시작 대기중</div>";
		else if( data.BATCH_STATE == "start")		html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>시작</div>";
		else if( data.BATCH_STATE == "stop" )		html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>정지</div>";
		else 									html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>에러<div></td>";
		
		html += "	<td title="+data.createDataTime+">"+data.createDataTime+"</td>";
		// if( fnNotNullAndEmpty(data.ENROLLEMENT_ID) )
		// 	html += "	<td title="+data.ENROLLEMENT_ID+">"+data.ENROLLEMENT_ID+"</td>";
		// else
		// 	html += "	<td title="+data.USER_ID+">"+data.USER_ID+"</td>";
		html += "</tr>";
	}
	return html;
}

/*배치신청 목록 생성*/
// var fnCreateBatchRequestListHtml = function(list){
// 	var html = "";
// 	for( var i in list ){
// 		var data = list[i];
// 		var addBatchHtml = "<button class='button__primary' onclick=fnApprovalBatchRequest('"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"')>승인</button>";
// 		if( data.PROGRESS_STATE == "reject" ) addBatchHtml = "거절";
		
// 		if( data.PROGRESS_STATE != "done" ){
// 			html += "<tr>";
// 			html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'></label></div></td>";
// 			html += "	<td>"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"</td>";
// 			html += "	<td title="+data.NAME+">"+data.NAME+"</td>";
// 			html += "	<td title="+data.MAKE_DATA_METHOD+">"+data.MAKE_DATA_METHOD+"</td>";
// 			html += "	<td title="+data.modelName+">"+data.modelName+"</td>";
// 			html += "	<td title="+data.RESULT_UPDATE_DOMAIN_NAME+">"+data.RESULT_UPDATE_DOMAIN_NAME+"</td>";
// 			if( data.RESULT_UPDATE_METHOD == "replace" )	html += "	<td title=REPLACE>REPLACE</td>";
// 			else 	html += "	<td title=UPDATE>UPDATE</td>";
// 			html += "	<td title="+data.EXECUTION_CYCLE+">"+data.EXECUTION_CYCLE+"</td>";
// 			html += "	<td title="+data.createDataTime+">"+data.createDataTime+"</td>";
// 			html += "	<td title="+fnConvertProgressState(data.PROGRESS_STATE)+">"+fnConvertProgressState(data.PROGRESS_STATE)+"</td>";
// 			html += "	<td title="+data.USER_ID+">"+data.USER_ID+"</td>";
// 			html += "	<td>"+addBatchHtml+"</td>";
// 			html += "</tr>";
// 		}
// 	}
// 	return html;
// }

/*배치서버 목록 생성*/
// var fnCreateBatchServerListHtml = function(list){
// 	var html = "";
// 	for( var i in list ){
// 		var data = list[i];
// 		html += "";
// 		html += "<tr>";
// 		html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.INSTANCE_SEQUENCE_PK+"'><label for='"+data.INSTANCE_SEQUENCE_PK+"'></label></div></td>";
// 		html += "	<td><a class='js-modal-show' href='#templateInfo_modal' onclick='fnSetInfoModal(\""+data.CLOUD_INSTNACE_SERVER_ID+"\")'>"+data.NAME+"</a></td>";
// 		html += "	<td>"+data.AVAILABILITY_ZONE+"</td>";
// 		if( data.SERVER_STATE.indexOf('call') > -1 ){
// 			html += "	<td><div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
// 					"			<div class='progress' style='margin-bottom:0px;'>" +
// 					"				<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>"+convertServerState(data.SERVER_STATE)+"중</div></div></td>";
// 		}else{
// 			html += "	<td><div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertServerState(data.SERVER_STATE)+"</div></td>";
// 		}
		
// 		if( data.MODULE_STATE == "checking" ){
// 			html += "	<td><div class='moduleState' data-serverState="+data.MODULE_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
// 			"			<div class='progress' style='margin-bottom:0px;'>" +
// 			"				<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>체크중</div></div></td>";
			
// 		}else{
// 			html += "	<td><div class='moduleState' data-serverState="+data.MODULE_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertModuleState(data.MODULE_STATE)+"</div></td>";			
// 		}
		
// 		html += "</tr>";
// 	}
// 	return html;
// }

/*배치로그 목록 생성*/
var fnCreateBatchLogListHtml = function(list){

	var html = "";
	for( var i in list ){
		var data = list[i];
		
		if(data.codename != undefined){
			html += "";
			html += "<tr>";
			html += "	<td title='"+fnReplaceNull(data.batchName)+"'>"+fnReplaceNull(data.batchName)+"</td>";
			html += "	<td title='"+data.codename+"'>"+data.codename+"</td>";
			html += "	<td title='"+fnReplaceNull(data.batchStartDateTime)+"'>"+fnReplaceNull(data.batchStartDateTime)+"</td>";
			html += "	<td title='"+fnReplaceNull(data.batchEndDateTime)+"'>"+fnReplaceNull(data.batchEndDateTime)+"</td>";
			if( data.BATCH_IS_SUCCESS == true || data.BATCH_IS_SUCCESS == "true" ) 
			html += "	<td>성공</td>";
			else 	html += "	<td>실패</td>";
			
			html += "	<td title='"+data.createDataTime+"'>"+data.createDataTime+"</td>";
			html += "	<td><button class='button__primary' onclick=fnBatchLogDetail('"+data.LOG_BATCH_SEQUENCE_PK+"')>상세</button></td>";
			html += "</tr>";
			
		}
	}
	return html;
}


// 등록할 배치서버 가져오기
var fnGetBatchServerList = function(){
	var html = "";
	var serverList = fnGetBatchServerListByAjax();
	for( var i in serverList ){
		html += "<option value="+serverList[i].INSTANCE_SEQUENCE_PK+">"+serverList[i].NAME+"</option>";
	}
	$("#batchInstanceSequenceFk2").html(html);
}

/*배치신청 승인여부 모달*/
var fnApprovalBatchRequest = function(batchRequestPk){
	// 등록할 배치서버 가져오기
	fnGetBatchServerList();
	if( $("#batchInstanceSequenceFk2").val() == null ){
		fnComNotify("warning", "배치서버를 생성해주세요.");
		$('a[href="#tab_batchServerManage"]').click();
		return false;
	}
	
	$("#batchForm").find("input").each(function(){
		$(this).val("");
	});
	$("#userRequestTerm").val("");
	$(".modalName").text("승인");
		
	// 사용자별 프로젝트, 모델 목록 가져오기
	fnGetProjectOfUserId();
	
	// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
	fnGetRequestTemplateAvailable();
	
	var batchRequest = fnGetbatchServiceRequestByAjax(batchRequestPk);
	
	$("#name").val(batchRequest.NAME); // 배치명
	// 프로젝트
	$("#selectedProject li").each(function(){
		var projectSequencePk = $(this).attr("data-projectSequencePk");
		if( projectSequencePk == batchRequest.PROJECT_SEQUENCE_FK3 ){
			fnGetModelsOfProjectPk(batchRequest.PROJECT_SEQUENCE_FK3, "useOfBatch");
			$(".projectList").removeClass("active");
			$(this).addClass("active");
		}
	});
	// 모델
	$("#selectedModel li").each(function(){
		var modelSequenceFk1 = $(this).attr("data-modelSequenceFk1");
		if( modelSequenceFk1 == batchRequest.MODEL_SEQUENCE_FK1 ){
			$(".modelList").removeClass("active");
			$(this).addClass("active");
		}
	});
	
	var executionCycleArr = batchRequest.EXECUTION_CYCLE.split(" ");
	for( var i in executionCycleArr ){
		$("#executionCycle_"+i).val(executionCycleArr[i]); // 실행주기
	}

	batchServiceRequestSequencePk = batchRequest.BATCH_SERVICE_REQUEST_SEQUENCE_PK; // 배치신청 번호
	$("#nifiTemplateName").val(batchRequest.NIFI_TEMPLATE_NAME); // NIFI 템를릿명
	$("#resultUpdateDomain").val(batchRequest.RESULT_UPDATE_DOMAIN_ID); // 도메인명
	$("#executionCycle").val(batchRequest.EXECUTION_CYCLE); // 실행주기
	$("#resultUpdateMethod").val(batchRequest.RESULT_UPDATE_METHOD); // 결과만영방식
	$("#enrollmentTerm").val(batchRequest.ENROLLMENT_TERM); // 요청사항
	$("#enrollementId").val(batchRequest.USER_ID); // 요청자
	$("#storeMethod").val(batchRequest.STORE_METHOD); // 저장방법
	$("#totalColumnName").val(batchRequest.TOTAL_COLUMN_NAME); // 전체값을 포함하는 컬럼이름
	$("#domainIdColumnName").val(batchRequest.DOMAIN_ID_COLUMN_NAME); // 도메인컬럼이름
	$("#makeDataMethod").val(batchRequest.MAKE_DATA_METHOD); // 데이터 생성방식
	$("#sql").val(batchRequest.SQL); //데이터 생성 방식 중 SQL 선택 시 SQL 구문
	$("#targetType").val(batchRequest.TARGET_TYPE); // 후처리 시 타깃 처리방식
	$("#datasetId").val(batchRequest.DATASET_ID); // 코어모듈 저장 시 데이터셋 아이디


	showMakeDataMethod();
	showStoreMethod();

	$(".rejectBtn").show();
	$(".registDiv").show();
	fnOpenModal("batchModal");
	
}

/*배치 등록/수정*/
var fnSaveBatch = function(){
	// validation
	if( $.trim($("#name").val()) == "" ){
		fnComNotify("warning", "배치명을 입력해주세요.");
		$("#name").focus();
		return false;
		
	}else if(  $.trim($("#nifiTemplateName").val()) == "" && $.trim($("#sql").val()) == ""){
		fnComNotify("warning", "NIFI 템플릿 또는 SQL을 입력해주세요.");
		$("#nifiTemplateName").focus();
		return false;

	}else if( $.trim($("#domainIdColumnName").val()) == "" ){
		fnComNotify("warning", "도메인컬럼 이름을 입력해주세요.");
		$("#domainIdColumnName").focus();
		return false;
		
	}
	else if($("#updateAttribute li.active").length<=0){
		fnComNotify("warning", "업데이트 하는 속성이 적어도 하나 이상 선택되어야 합니다.");
		$("#updateAttribute").focus();
		return false;

	}
	else if( $.trim($("#applyDataPath").val()) == "" ){
		fnComNotify("warning", "파일 생성 위치를 입력해주세요.");
		$("#applyDataPath").focus();
		return false;
		
	}else if( $.trim($("#applyDataNameRule").val()) == "" ){
		fnComNotify("warning", "파일 생성 규칙을 입력해주세요.");
		$("#applyDataNameRule").focus();
		return false;
		
		/*날짜 포멧 체크*/
	}else if( $.trim($("#applyDataNameRule").val()) == "" ){
		fnComNotify("warning", "파일 생성규칙을 입력해주세요.");
		$("#applyDataNameRule").focus();
		return false;
	}else{
		var executionCycle = "";
		/*실행주기 조합*/
		for(var i=0; i<5; i++ ){
			if($.trim($("#executionCycle_"+i).val()) == ""){
				fnComNotify("warning", "실행주기를 입력해주세요.");
				$("#executionCycle_"+i).focus();
				return false;
			}
			// 실행주기 한글 금지
			if( fnCheckKorean($("#executionCycle_"+i).val()) ){
				fnComNotify("warning", "한글은 입력불가능합니다.");
				$("#executionCycle_"+i).focus();
				return false;
			}
			if( i == 0 ) executionCycle = $("#executionCycle_"+i).val();
			else  executionCycle += " "+$("#executionCycle_"+i).val();
		}
		
		/*파일 생성위치 체크*/
		var applyDataPath = $("#applyDataPath").val();
		if( applyDataPath.substring(0,1) != "/" ) applyDataPath = "/" + applyDataPath;
		if( applyDataPath.substring(applyDataPath.length-1) != "/" ) applyDataPath = applyDataPath + "/";
		
		var sandboxInstanceSequenceFk1, projectSequenceFk3, modelSequenceFk4;
		$("#selectedModel").find("li").each(function(){
			if( $(this).hasClass("active") ){
				modelSequenceFk4 = $(this).attr("data-modelSequenceFk1");
				sandboxInstanceSequenceFk1 = $(this).attr("data-instanceSequenceFk2");
				projectSequenceFk3 = $(this).attr("data-projectSequenceFk3");
			}
		});
		var type = $(".modalName").first().text();
		
		if( type != "수정" && modelSequenceFk4 == undefined ){
			fnComNotify("warning","선택된 모델이 없습니다.");
			return false;
		}

		var updateAttribute="";
		$("#updateAttribute li.active").each(function (){
			updateAttribute+=(','+$(this).data("attribute")); });


		var data = {
			"name" : $("#name").val()
			,"sandboxInstanceSequenceFk1" : sandboxInstanceSequenceFk1
			,"batchInstanceSequenceFk2" : $("#batchInstanceSequenceFk2").val()
			,"projectSequenceFk3" : projectSequenceFk3
			,"modelSequenceFk4" : modelSequenceFk4
			,"nifiTemplateName" : $("#nifiTemplateName").val()
			,"applyDataPath" : $("#applyDataPath").val()
			,"applyDataNameRule" : $("#applyDataNameRule").val()
			,"resultUpdateDomainId" : $("#resultUpdateDomain").val()
			,"resultUpdateDomainName" : $("#resultUpdateDomain option:selected").text()
			,"executionCycle" : executionCycle
			,"resultUpdateMethod" : $("#resultUpdateMethod").val()
			,"enrollmentTerm" : $("#enrollmentTerm").val()
			,"enrollementId" : $("#enrollementId").val()
			,"storeMethod" : $("#storeMethod").val()

			,"makeDataMethod" : $("#makeDataMethod").val()
			,"sql" : $("#sql").val()
			,"targetType" : $("#targetType").val()
			,"datasetId" : $("#datasetId").val()

			,"totalColumnName" : $("#totalColumnName").val()
			,"domainIdColumnName" : $("#domainIdColumnName").val()
			,"updateAttribute" :updateAttribute
		};



		// 등록
		var url = "/batchServices";
		var method = "POST";
		
		if( type == "수정" ){
			url = "/batchServices/"+batchServiceSequencePk;
			method = "PATCH";
			data["batchServiceSequencePk"] = batchServiceSequencePk;
			
		}else if( type == "승인" ){
			data["batchServiceRequestSequencePk"] = batchServiceRequestSequencePk;
		}

		if( confirm(type+" 하시겠습니까?") ){
			var response = fnbatchServicesByAjax(url, method, data);
			if( response.result == "success" ){
				/*배치신청 테이블 Row 삭제*/
				if( type == "승인" )	$("#logTable_batchRequestList").dataTable().fnDeleteRow(clickRow);
				
				fnUpdateBatchTable(response.batchService, type);
				fnComNotify("success", "배치를  "+type+"하였습니다.");
				fnCloseModal("batchModal");
				
			}else if( response.result == "fail" && response.detail == "duplicateName" ){
				$("#name").focus();
				fnComNotify("warning","배치명이 중복되었습니다.");
				
			}else{
				fnComErrorMessage("배치 "+type+" 에러!!", response.detail);
			}
		}
	}
}

/*업데이트 배치목록 테이블*/
var fnUpdateBatchTable = function(data, option){
	var checkbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_SEQUENCE_PK+"'></label></div>";
	var name = "<a class='js-modal-show' href='#batchModal'>"+data.NAME+"</a>";
	
	var batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>에러</div>";
	if( data.BATCH_STATE == "success" )		batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>성공</div>";
	else if( data.BATCH_STATE == "stop" && data.USE_FLAG == "true")		batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>시작 대기중</div>";
	else if( data.BATCH_STATE == "start")		batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>시작</div>";
	else if( data.BATCH_STATE == "stop" )		batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>정지</div>";
	else batchState = "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>에러<div></td>";
	
    var resultUpdateMethod = "UPDATE";
    if( data.RESULT_UPDATE_METHOD == "replace" ) resultUpdateMethod = "REPLACE";
    
    var userId
    if( fnNotNullAndEmpty(data.ENROLLEMENT_ID) )	userId = data.ENROLLEMENT_ID
	else	userId = data.USER_ID
	
    
	if( option == "등록" || option == "승인" ){
		var num = $("#logTable_batchList").DataTable().rows().count()+1;
		$("#logTable_batchList").dataTable().fnAddData([
			checkbox, data.BATCH_SERVICE_SEQUENCE_PK, name, data.projectName, data.modelName, data.MAKE_DATA_METHOD
			, data.RESULT_UPDATE_DOMAIN_NAME, resultUpdateMethod, data.EXECUTION_CYCLE, batchState, data.createDataTime
		]);
		$("#logTable_batchList").DataTable().order([1, "desc"]).draw();
		
	}else{
		$("#logTable_batchList").dataTable().fnUpdate([
			checkbox, data.BATCH_SERVICE_SEQUENCE_PK, name, data.projectName, data.modelName, data.MAKE_DATA_METHOD
			, data.RESULT_UPDATE_DOMAIN_NAME, resultUpdateMethod, data.EXECUTION_CYCLE, batchState, data.createDataTime
		], clickRow);
	}
}

/*Nifi Admin 새창*/
var fnNewPageForAdminNifi = function(){
	var response = fnGetAdminNifiUrl();
	if(response!=null){
		console.log(response);
		window.open(response.nifiUrl);
	}else {
		fnComNotify("warning", "등록된 관리자를 위한 NIFI가 없습니다.");
	}
};

/*Nifi, Hue 새창*/
var fnNewPage = function(instanceSequencePk, type){
	var url = fnSandboxSetInstancePkInSessionByAjax(instanceSequencePk);

	alert(type);
	if(type == "Nifi"){
		url = url + "/nifi/";
	} else if(type == "Hue"){
		url = url + "/hue/";
	}else{
		url = url + "/unknown/";
	}

	window.open(url);
};


var fnOpenPageForNifi = function(){
	var instanceSeq=$("#selectedModel .active").data("instancesequencefk2");
	if(instanceSeq!=null){
		fnNewPage(instanceSeq, "Nifi");
	}else{
		fnComNotify("warning", "모델을 먼저 선택하세요.");
	}
};

/*배치 시작 & 정지*/
var fnStartAndStopBatch = function(option){
	// 체크된 항목 가져오기
	var checkMap = fnTableCheckList("batchTbodyHtml");
	var checkIdList = checkMap.checkIdList;
	var checkRowList = checkMap.checkRowList;
	var successFlug = false;
	var comment = option=='start' ? "시작" : "정지";
	var useFlag = option=='start' ? true : false;
	
	if( checkIdList.length > 0 ){
		if( confirm(comment+" 하시겠습니까?") ){
			for( var i in checkIdList ){
				var data = {
						"useFlag" : useFlag
						,"batchServiceSequencePk" : checkIdList[i]
						,"batchState" : option
					};
				var response = fnStartAStopBatchByAjax(data);
				if( response.result == "success" ){
					fnComNotify("success", "배치를 "+comment+"하였습니다.");
					successFlug = true;
					/* 테이블 변경 */
					clickRow = checkRowList[i];
					fnUpdateBatchTable(response.batchService);
				}else{
					fnComErrorMessage("배치 "+comment+" 에러!!", response.detail);
				}
			}
			
			if( successFlug ){
				fnUnCheckbox("check-all_batchList");
			}
		}
		
	}else{
		fnComNotify("warning", comment+"할 목록을 선택해주세요.");
	}
}

/*배치 삭제*/
var fnDeleteBatch = function(){
	// 체크된 항목 가져오기
	var checkMap = fnTableCheckList("batchTbodyHtml");
	var checkIdList = checkMap.checkIdList;
	var checkRowList = checkMap.checkRowList;
	var successFlug = false;
	if( checkIdList.length > 0 ){
		if( confirm("삭제 하시겠습니까?") ){
			for( var i in checkIdList ){
				var response = fnDeleteBatchByAjax(checkIdList[i]);
				if( response.result == "success" ){
					fnComNotify("success", "배치를 삭제하였습니다.");
					successFlug = true;
				}else{
					fnComErrorMessage("배치 삭제 에러!!", response.detail);
				}
			}
			
			/* 테이블 삭제 */
			if( successFlug ){
				fnComDeleteTable("logTable_batchList", checkRowList);
			}
		}
		
	}else{
		fnComNotify("warning", "삭제할 목록을 선택해주세요.");
	}
}
/*배치 신청 삭제*/
var fnDeleteBatchRequest = function(){
	// 체크된 항목 가져오기
	var checkMap = fnTableCheckList("batchRequestTbodyHtml");
	var checkIdList = checkMap.checkIdList;
	var checkRowList = checkMap.checkRowList;
	var successFlug = false;
	if( checkIdList.length > 0 ){
		if( confirm("배치신청을 삭제 하시겠습니까?") ){
			for( var i in checkIdList ){
				var response = fnDeleteBatchRequestByAjax(checkIdList[i]);
				if( response.result == "success" ){
					fnComNotify("success", "배치신청을 삭제하였습니다.");
					successFlug = true;
				}else{
					fnComErrorMessage("배치신청 삭제 에러!!", response.detail);
				}
			}
			/* 테이블 삭제 */
			if( successFlug ){
				fnComDeleteTable("logTable_batchRequestList", checkRowList);
			}
		}
		
	}else{
		fnComNotify("warning", "삭제할 목록을 선택해주세요.");
	}
}

/*배치신청 거절*/
var fnRejectBatch = function(){
	var data = {
		"batchServiceRequestSequencePk" : batchServiceRequestSequencePk
		,"progressState" : "reject"
	};
	
	var url = "/batchServiceRequests/"+batchServiceRequestSequencePk;
	var method = "PATCH";

	if( confirm("신청을 거절 하시겠습니까?") ){
		var response = fnbatchServiceRequestsByAjax(url, method, data);
		if( response.result == "success" ){
			/*배치신청 테이블 Row 삭제*/
			fnUpdateBatchRequestTable(response.batchServiceRequest);
			fnComNotify("success", "배치신청을  거절하였습니다.");
			fnCloseModal("batchModal");
			
		}else{
			fnComErrorMessage("배치 "+type+" 에러!!", response.detail);
		}
	}
}

/*업데이트 배치신청목록 테이블*/
var fnUpdateBatchRequestTable = function(data){
	var checkbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'></label></div>";
	var addBatchHtml = "<button class='button__primary' onclick=fnApprovalBatchRequest('"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"')>승인</button>";
	if( data.PROGRESS_STATE == "reject" ) addBatchHtml = "거절";
	
    var resultUpdateMethod = "UPDATE";
    if( data.RESULT_UPDATE_METHOD == "replace" ) resultUpdateMethod = "REPLACE";
    
	$("#logTable_batchRequestList").dataTable().fnUpdate([
		checkbox, data.BATCH_SERVICE_REQUEST_SEQUENCE_PK, data.NAME,  data.MAKE_DATA_METHOD
		, data.modelName, data.RESULT_UPDATE_DOMAIN_NAME, resultUpdateMethod, data.EXECUTION_CYCLE, data.createDataTime
		, fnConvertProgressState(data.PROGRESS_STATE), data.USER_ID, addBatchHtml
	], clickRow);
}

/*배치 서버사양 가져오기*/
var fnSetInfoModal = function(serverId,option){
	$(".infoModalTd").text("");
	var response = fnGetInstanceServerByAjax(serverId);
	var id = "";
	if( option == "createInstance" ) id="c_";
	
	$("#"+id+"infoVcpus").text(response.vcpus);
	$("#"+id+"infoRam").text(numberWithCommas(response.ram)+" MB");
	$("#"+id+"infoRootDisk").text(response.disk+" GB");
	$("#"+id+"infoEDisk").text(response.eDisk+" GB");
	$("#"+id+"infoAllDisk").text(Number(response.disk)+Number(response.eDisk)+" GB");
	
	if( option == undefined )	fnOpenModal("templateInfo_modal");
}

/*업데이트 배치서버 목록 테이블*/
var fnUpdateBatchServerTable = function(data){
	var checkbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.INSTANCE_SEQUENCE_PK+"'><label for='"+data.INSTANCE_SEQUENCE_PK+"'></label></div>";
	var name = "<div data-toggle='modal' data-target='.infoModal' role='button' onClick='fnSetInfoModal(\""+data.CLOUD_INSTNACE_SERVER_ID+"\")'>"+data.NAME+"</div>";
	var serverState = data.SERVER_STATE;
	var moduleState = data.MODULE_STATE;
	if( serverState.indexOf('call') > -1 ){
		serverState = "<div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
		  "		<div class='progress' style='margin-bottom:0px;'>" +
		  "			<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>"+convertServerState(data.SERVER_STATE)+"중</div></div>";
	}else{
		serverState = "<div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertServerState(data.SERVER_STATE)+"</div>";
	}
	
	if( moduleState == "checking" ){
		moduleState = "<div class='moduleState' data-serverState="+data.MODULE_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
		  "		<div class='progress' style='margin-bottom:0px;'>" +
		  "			<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>체크중</div></div>";
	}else{
		moduleState = "<div class='moduleState' data-moduleState='"+data.MODULE_STATE+"' data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertModuleState(data.MODULE_STATE)+"</div>"
	}
	
	var privateIp = "";
	if( fnNotNullAndEmpty(data.PRIVATE_IP) )
		privateIp = "	<div class='privateIp' data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+data.PRIVATE_IP+"</div>";
	else 	privateIp = "	<div class='privateIp' data-pk='"+data.INSTANCE_SEQUENCE_PK+"'></div>";
	
	var num = $("#logTable").DataTable().rows().count()+1;
	
	$("#logTable").dataTable().fnAddData([
		checkbox, name, data.AVAILABILITY_ZONE, serverState, moduleState
	]);
	
	$("#logTable").DataTable().order([1, "desc"]).draw();
}

/*배치서버 상태값 갱신*/
var fnChangeBatchServerState = function(){
	/*배치서버 가져요기*/
	var batchServers = fnGetBatchServerListByAjax();
	for( var i in batchServers ){
		var data = batchServers[i];
		// 상태값 변경
		$(".serverState").each(function(){
			if(  $(this).attr("data-pk") == data.INSTANCE_SEQUENCE_PK 
					&& $(this).attr("data-serverState") != data.SERVER_STATE ){
				var serverStateHtml = ""
				if( data.SERVER_STATE.indexOf("_done") > -1 ){
					serverStateHtml = "<div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertServerState(data.SERVER_STATE)+"</div>";
				}else{
					serverStateHtml = "<div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
	  				  				  "		<div class='progress' style='margin-bottom:0px;'>" +
	  				  				  "			<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>"+convertServerState(data.SERVER_STATE)+"중</div></div>";
				}
				$(this).parent().html(serverStateHtml);
			}
			
		});

		// 모듈 상태값 변경 
		$(".moduleState").each(function(){
			if( $(this).attr("data-moduleState") != data.MODULE_STATE 
				&& $(this).attr("data-pk") == data.INSTANCE_SEQUENCE_PK ){
					var moduleStateHtml = "";
					if( data.MODULE_STATE == "checking" ){
						moduleStateHtml = "<div class='moduleState' data-serverState="+data.MODULE_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
		  				  "		<div class='progress' style='margin-bottom:0px;'>" +
		  				  "			<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>체크중</div></div>";
					}else{
						var moduleStateHtml = "<div class='moduleState' data-moduleState='"+data.MODULE_STATE+"' data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertModuleState(data.MODULE_STATE)+"</div>"	
					}
					
					$(this).html(moduleStateHtml);
			}
		});
	}
}

/*배치실행상태 체크*/
var fnChangeBatchState = function(){
	/*배치 목록 가져요기*/
	var batchList = fnGetBatchServicesByAjax();
	for( var i in batchList ){
		var data = batchList[i];
		// 상태값 변경
		$(".batchState").each(function(){
			if(  $(this).attr("data-pk") == data.BATCH_SERVICE_SEQUENCE_PK 
					&& $(this).attr("data-batchState") != data.BATCH_STATE ){
				var batchStateHtml = ""
				if( data.BATCH_STATE == "success" ){
					batchStateHtml = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>성공</div>";
				}else{
					batchStateHtml = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>에러</div>";
				}
				$(this).parent().html(batchStateHtml);
			}
		});
	}
}

/*배치서버생성 모달 */
var fnCreateBatchModal = function(){
	fnOpenModal("createBatchModal");
}

/*배치서버생성*/
var fnCreateBatch = function(){
	if( $.trim($("#batchServerName").val()) == "" ){
		fnComNotify("warning", "배치서버명을 입력해주세요.");
		$("#batchServerName").focus();
		return false;
		
	}else{
		var data = {
				"name" : $("#batchServerName").val()
		}
		if( confirm("배치서버를 생성하시겠습니까?") ){
			var response = fnCreateBatchServerByAjax(data);
			if( response.result == "success" ){
				$("#batchServerName").val("");
				fnCloseModal("createBatchModal");
				fnUpdateBatchServerTable(response.batchServer);
				fnUnCheckbox("check-all_batchList");
				fnComNotify("success", "배치서버 생성 하였습니다.");
				
			}else if( response.detail == "duplicateName"){
				$("#instanceName").focus();
				fnComNotify("warning","배치서버명이 중복되었습니다.");
			
			}else if( response.detail == "disk is smaller than the minimum"){
				fnComNotify("warning","디스크가 스냅샷이미지보다 작습니다.");
				
			}else if( response.detail == "Quota exceeded for ram:"){
				fnComNotify("warning","배치서버 허용 메모리를 초과하였습니다.");
			
			}else if( response.detail == "Quota exceeded for cores:"){
				fnComNotify("warning","배치서버 허용 CPU를 초과하였습니다.");
				
			}else{
				fnComErrorMessage("배치서버 생성 에러!!", response.detail);
			}			
		}
	}	
}

/*배치 이력 목록 조회*/
var fnSearchBatchLog = function(){
     $("#logBatchTable").dataTable().fnDestroy();
	  var columns = ["LOG_BATCH_SEQUENCE_PK","batchName","codename","batchStartDateTime","batchEndDateTime","BATCH_IS_SUCCESS","createDataTime","detail"];
	  $("#logBatchTable").DataTable( {
		  	"language" : language
		  	,'order': [[ 0, 'desc' ]]
		  	,bSortable: true
			,bPaginate: true
			,bLengthChange: true
			,responsive: true
			,bAutoWidth: false
			,processing: false
			,ordering: true
			,bServerSide: true
			,searching: true
			,sAjaxSource: "/UI/batchLogs?startDate="+$("#startDate").val()+"&endDate="+$("#endDate").val()+"&columns="+columns
			// ,sAjaxSource: "/UI/batchLogs"
			// ,sAjaxSource: "/UI/batchLogs" ,data:{"startDate":$("#startDate").val(), "endDate":$("#endDate").val(), "columns":columns}
			// ,fnServerData: function(aodata){
			// 	addata.push({"startDate":$("#startDate").val(), "endDate":$("#endDate").val(), "columns":columns});
			// }
			,sServerMethod: "POST"
			,columns: [
				{data: "LOG_BATCH_SEQUENCE_PK"}
				,{data: "batchName"}
				,{data: "codename"}
				,{data: "batchStartDateTime"}
				,{data: "batchEndDateTime"}
				,{data: "BATCH_IS_SUCCESS"}
				,{data: "createDataTime"}
				,{data: "detail"}
			]
	  		,columnDefs: [
	  			{
	  				"targets": 5
	  				,"render": function(BATCH_IS_SUCCESS){
	  					var batchIsSuccess = "실패";
	  					if( BATCH_IS_SUCCESS == true || BATCH_IS_SUCCESS == "true" ) 
	  						batchIsSuccess = "성공";
	  					
	  					return batchIsSuccess;
	  				}
	  			},
			  {
				  "targets": [3,4,6]
				  ,"render": function(time){
					  return time.value;
				  }
			  }
	  			,{
	  				"targets": 7
	  				,"render": function(LOG_BATCH_SEQUENCE_PK){
	  					return "<button class='button__primary' onclick=fnBatchLogDetail('"+LOG_BATCH_SEQUENCE_PK+"')>상세</button>";
	  				}
	  			}
	  		]
	  } );
	  
	  $('.dataTables_filter').hide();
}

/*배치로그 상세*/
var fnBatchLogDetail = function(logBatchSequencePk){
	var batchLog = fnGetBatchLogByAjax(logBatchSequencePk);
	$("#batchLogForm").find("input").each(function(){
		$(this).val("-");
	});
	$("#BATCH_IS_SUCCESS").val("실패");
	$("#BATCH_TRANSFER_IS_SUCCESS").val("실패");
	$("#BATCH_RESULT").css("height","100%").css("overflow","hidden").html("<input type='text' class='form-control' value='-' readonly>");
	$("#TRANSFER_DATA").css("height","100%").css("overflow","hidden").html("<input type='text' class='form-control' value='-' readonly>");

	$.each(batchLog, function(key, value){
		if( key == "BATCH_RESULT" ){
			$("#BATCH_RESULT").css("height","200px").css("overflow","scroll").html("<pre>"+JSON.stringify(value,null,2)+"</pre>");
			
		}else if( key == "TRANSFER_DATA" ){
			$("#TRANSFER_DATA").css("height","200px").css("overflow","scroll").html("<pre>"+JSON.stringify(value,null,2)+"</pre>");
				
		}else if( key == "createDataTime" || key == "batchStartDateTime" || key == "batchEndDateTime"){
			$("#"+key).val(value.value);
		}else if( key == "BATCH_IS_SUCCESS" || key == "BATCH_TRANSFER_IS_SUCCESS"){
			if( value == true || value == "true")	$("#"+key).val("성공");
		}else if( fnNotNullAndEmpty(value) ){
			$("#"+key).val(value);
		}
	});
	
	fnOpenModal("batchLogModal");
}


























