var clickRow, batchServiceRequestSequencePk, batchServiceSequencePk, modalType;
var userRole = $("#userRole").val();
$("#loading").show();

$(function(){
	fnInit();
	
	/* 10초 주기로 반복 조회 후 상태값 변경 */
	setInterval(function(){
		fnChangeBatchState();/*배치실행상태 체크*/
	},30000);
	
	// 배치신청 등록 버튼 클릭시
	$(document).on("click", "#addBatchRequestModalBtn", function(){
		
		// 사용자별 프로젝트, 모델 목록 가져오기
		fnGetProjectOfUserId();
		if( $("#selectedProject li").length == 0 ){
			fnComNotify("warning", "배치서버를 생성해주세요.");
			fnN2MCloseModal("#batchModal");
			return false;
		}
		if( $("#selectedModel li").length == 0 ){
			fnComNotify("warning", "프로젝트에서 모델을 생성해주세요.");
			fnN2MCloseModal("#batchModal");
			return false;
		}
		
		$("#batchRequestForm").find("input").each(function(){
			$(this).val("");
		});
		$("#userRequestTerm").val("");

		$("#makeDataMethod").val("nifi"); //  데이터 생성방식
		$("#sql").val(""); // 데이터 생성 방식 중 SQL 선택 시 SQL 구문
		$("#targetType").val("rawData"); // 후처리 시 타깃 처리방식
		$("#datasetId").val(""); // 코어모듈 저장 시 데이터셋 아이디
		$("#totalColumnName").val(""); // 전체값을 포함하는 컬럼이름

		showMakeDataMethod();
		showStoreMethod();

		modalType = "batchRequest";
		$(".modalName").text("신청");
		$(".registDiv").show();
		$("#saveBatchRequestBtn").show();
		$("#saveBatchRequestText").hide();
		
		// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
		fnGetRequestTemplateAvailable();
			
	});
	
	// 배치신청 모달 > 프로젝트  클릭시
	$(document).on("click", ".projectList", function(){
		$(".projectList").removeClass("active");
		$(this).addClass("active");
		fnGetModelsOfProjectPk($(this).attr("data-projectSequencePk"));
	});
	
	// 모델  클릭시
	$(document).on("click", ".modelList", function(){
		$(".modelList").removeClass("active");
		$(this).addClass("active");
	});
	
	/* 배치 목록 클릭시 */
	$(document).on("click", "#batchTbodyHtml td", function(){
		if( $(this).index() == 1 ){
			var clickRows = $("#logTable_batchList").dataTable().fnGetPosition(this); // 변경하고자 하는 clickRow
			clickRow = clickRows[0];
			var data = $("#logTable_batchList").dataTable().fnGetData($(this).parent());
			batchServiceSequencePk = data[1];
			modalType = "batch";
			$(".modalName").text("수정");
			
			// 사용자별 프로젝트, 모델 목록 가져오기
			fnGetProjectOfUserId();
			
			// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
			fnGetRequestTemplateAvailable();
			
			var batch = fnGetBatchServiceByAjax(batchServiceSequencePk);
			
			$(".registDiv").hide();
			$("#saveBatchRequestBtn").hide();
			$("#saveBatchRequestText").hide();
			fnSetBatchModal(batch); // 배치모달 데이터 세팅
		}
	});
	
	/* 배치신청 목록 클릭시 */
	$(document).on("click", "#batchRequestTbodyHtml td", function(){
		if( $(this).index() == 1 ){
			var clickRows = $("#logTable_batchRequestList").dataTable().fnGetPosition(this); // 변경하고자 하는 clickRow
			clickRow = clickRows[0];
			var data = $("#logTable_batchRequestList").dataTable().fnGetData($(this).parent());
			batchServiceRequestSequencePk = data[1];
			modalType = "batchRequest";
			$(".modalName").text("수정");
			
			// 사용자별 프로젝트, 모델 목록 가져오기
			fnGetProjectOfUserId();
			
			// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
			fnGetRequestTemplateAvailable();
			
			var batchRequest = fnGetbatchServiceRequestByAjax(batchServiceRequestSequencePk);

			if( "standby" == batchRequest.PROGRESS_STATE ){
				$("#saveBatchRequestBtn").show();
				$("#saveBatchRequestText").hide();
				
			}else{
				$("#saveBatchRequestBtn").hide();
				$("#saveBatchRequestText").show();
			}
			
			fnSetBatchModal(batchRequest); // 배치모달 데이터 세팅
		}

	});
	
	/* 배치로그 목록 클릭시 */
	$(document).on("click", "#batchLogTbodyHtml td", function(){
		if( $(this).index() == 6 ){
			var data = $("#logBatchTable").dataTable().fnGetData($(this).parent());
			/*배치로그 상세*/
			fnBatchLogDetail(data.LOG_BATCH_SEQUENCE_PK);
		}
	});

	$(document).on("change", "#makeDataMethod", function(){
		showMakeDataMethod();
	});

	$(document).on("change", "#storeMethod", function(){
		showStoreMethod();
	});
		
})

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

var createTable = function(){
	  $("#logTable_batchList").DataTable( {
		  "language" : language
		  ,"autoWidth": false
	  } );	//DataTable
	  $('#logTable_batchList').DataTable().columns([1]).visible(false);
		
	  $("#logTable_batchRequestList").DataTable( {
		  "language" : language
		  ,"autoWidth": false
	  } );	//DataTable
	  $('#logTable_batchRequestList').DataTable().columns([1]).visible(false);
	
	  /*배치 이력 목록*/
	  fnSearchBatchLog();
	  
	$('.dataTables_filter').hide();
	$('.dataTables_length').hide();
	$('.dataTables_filter_custom').hide();
}

var fnInit = function(){
	fnSetDatepicker("startDate","endDate");
	fnSearch();
}

/*배치 목록 조회*/
var fnSearch = function(){
	// 배치 목록 조회
	var batchList = fnGetBatchServicesByAjax();
	$("#batchTbodyHtml").html(fnCreateHtml(batchList, "batch"));
	
	// 배치 신청 목록 조회
	var batchRequestList = fnGetbatchServiceRequestsByAjax();
	$("#batchRequestTbodyHtml").html(fnCreateHtml(batchRequestList, "batchRequest"));

	$("#loading").hide();
}

/*배치신청 목록 생성*/
var fnCreateHtml = function(list, option){
	var html = "";
	for( var i in list ){
		var data = list[i];
		if( data.PROGRESS_STATE != "done" ){
			html += "<tr>";
			if( option == "batch" ){
				html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_SEQUENCE_PK+"'></label></div></td>";
				html += "	<td>"+data.BATCH_SERVICE_SEQUENCE_PK+"</td>";
			}else{
				html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'></label></div></td>";
				html += "	<td>"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"</td>";
			}
			html += "	<td><a class='js-modal-show' href='#batchModal' onclick='N2M.ui.toggleModal(\"#batchModal\")'>"+data.NAME+"</a></td>";
			html += "	<td title='"+data.projectName+"'>"+data.projectName+"</td>";
			html += "	<td title='"+data.modelName+"'>"+data.modelName+"</td>";
			html += "	<td title='"+data.MAKE_DATA_METHOD+"'>"+data.MAKE_DATA_METHOD+"</td>";
			html += "	<td title='"+data.RESULT_UPDATE_DOMAIN_NAME+"'>"+data.RESULT_UPDATE_DOMAIN_NAME+"</td>";
			if( data.RESULT_UPDATE_METHOD == "replace" )	html += "	<td>REPLACE</td>";
			else 	html += "	<td>UPDATE</td>";
			html += "	<td>"+data.EXECUTION_CYCLE+"</td>";
			
			if( option == "batch" ){
				if( data.USE_FLAG == "true" )		html += "	<td>시작완료</td>";
				else 								html += "	<td>정지</td>";
				
				if( data.BATCH_STATE == "success" )		html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>성공<div></td>";
				else 									html += "	<td><div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>에러<div></td>";
			}
			
			html += "	<td title='"+data.createDataTime+"'>"+data.createDataTime+"</td>";
			if( option == "batchRequest" )	html += "	<td>"+fnConvertProgressState(data.PROGRESS_STATE)+"</td>";
			html += "</tr>";
		}
	}
	return html;
}

/*배치모달 데이터 세팅*/
var fnSetBatchModal = function(data){
	console.log(data);
	$("#name").val(data.NAME); // 배치명
	// 프로젝트
	$("#selectedProject li").each(function(){
		var projectSequencePk = $(this).attr("data-projectSequencePk");
		if( projectSequencePk == data.PROJECT_SEQUENCE_FK3 ){
			fnGetModelsOfProjectPk(data.PROJECT_SEQUENCE_FK3);
			$(".projectList").removeClass("active");
			$(this).addClass("active");
		}
	});
	// 모델
	$("#selectedModel li").each(function(){
		var modelSequenceFk1 = $(this).attr("data-modelSequenceFk1");
		if( modelSequenceFk1 == data.MODEL_SEQUENCE_FK1 ){
			$(".modelList").removeClass("active");
			$(this).addClass("active");
		}
	});
	
	var executionCycleArr = data.EXECUTION_CYCLE.split(" ");
	for( var i in executionCycleArr ){
		$("#executionCycle_"+i).val(executionCycleArr[i]); // 실행주기
	}
	
	$("#nifiTemplateName").val(data.NIFI_TEMPLATE_NAME); // NIFI 템를릿명
	$("#storeMethod").val(data.STORE_METHOD); // 저장방법

	$("#makeDataMethod").val(""+data.MAKE_DATA_METHOD); // 데이터 생성방식
	$("#sql").val(""+data.SQL); // 데이터 생성 방식 중 SQL 선택 시 SQL 구문
	$("#targetType").val(""+data.TARGET_TYPE); //후처리 시 타깃 처리방식
	$("#datasetId").val(""+data.DATASET_ID); // 코어모듈 저장 시 데이터셋 아이디

	$("#totalColumnName").val(data.TOTAL_COLUMN_NAME); // 전체값을 포함하는 컬럼이름

	$("#domainIdColumnName").val(data.DOMAIN_ID_COLUMN_NAME); // 도메인컬럼이름
	$("#updateAttribute").val(data.UPDATE_ATTRIBUTE); // 업데이트하는 속성
	
	$("#resultUpdateDomain").val(data.RESULT_UPDATE_DOMAIN_ID); // 도메인명
	$("#resultUpdateMethod").val(data.RESULT_UPDATE_METHOD); // 결과만영방식
	if( fnNotNullAndEmpty(data.USER_REQUEST_TERM) )
		$("#userRequestTerm").val(data.USER_REQUEST_TERM); // 요청사항
	else
		$("#userRequestTerm").val(data.ENROLLMENT_TERM); // 요청사항

	showMakeDataMethod();
	showStoreMethod();
}


/*배치신청 등록/수정*/
var fnSaveBatchRequest = function(){
	// validation
	if( $.trim($("#name").val()) == "" ){
		fnComNotify("warning", "배치명을 입력해주세요.");
		$("#name").focus();
		return false;
		
	}else if( $.trim($("#nifiTemplateName").val()) == "" && $.trim($("#sql").val()) == "" ){
		fnComNotify("warning", "NIFI 템플릿 또는 SQL을 입력해주세요.");
		$("#nifiTemplateName").focus();
		return false;

	}else if( $.trim($("#domainIdColumnName").val()) == "" ){
		fnComNotify("warning", "도메인컬럼 이름을 입력해주세요.");
		$("#domainIdColumnName").focus();
		return false;
		
	}
	// else if( $.trim($("#updateAttribute").val()) == "" ){
	// 	fnComNotify("warning", "업데이트하는 속성을 입력해주세요.");
	// 	$("#updateAttribute").focus();
	// 	return false;
		
	// }
	else{
		var executionCycle = "";
		/*실행주기 조합*/
		for(var i=0; i<5; i++ ){
			if($.trim($("#executionCycle_"+i).val()) == ""){
				fnComNotify("warning", "실행주기를 입력해주세요.");
				$("#executionCycle_"+i).focus();
				return false;
			}
			if( i == 0 ) executionCycle = $("#executionCycle_"+i).val();
			else  executionCycle += " "+$("#executionCycle_"+i).val();
		}
		var modelSequenceFk1, instanceSequenceFk2, projectSequenceFk3;
		$("#selectedModel").find("li").each(function(){
			if( $(this).hasClass("active") ){
				modelSequenceFk1 = $(this).attr("data-modelSequenceFk1");
				instanceSequenceFk2 = $(this).attr("data-instanceSequenceFk2");
				projectSequenceFk3 = $(this).attr("data-projectSequenceFk3");
			}
		});
		
		var data = {
			"batchServiceRequestSequencePk" : batchServiceRequestSequencePk
			,"name" : $("#name").val()
			,"modelSequenceFk1" : modelSequenceFk1
			,"instanceSequenceFk2" : instanceSequenceFk2
			,"projectSequenceFk3" : projectSequenceFk3
			,"nifiTemplateName" : $("#nifiTemplateName").val()
			,"resultUpdateDomainId" : $("#resultUpdateDomain").val()
			,"resultUpdateDomainName" : $("#resultUpdateDomain option:selected").text()
			,"executionCycle" : executionCycle
			,"resultUpdateMethod" : $("#resultUpdateMethod").val()
			,"userRequestTerm" : $("#userRequestTerm").val()
			,"storeMethod" : $("#storeMethod").val()

			,"makeDataMethod" : $("#makeDataMethod").val()
			,"sql" : $("#sql").val()
			,"targetType" : $("#targetType").val()
			,"datasetId" : $("#datasetId").val()

			,"totalColumnName" : $("#totalColumnName").val()
			,"domainIdColumnName" : $("#domainIdColumnName").val()
			,"updateAttribute" : $("#updateAttribute").val()
		};
		
		var type = $(".modalName").first().text();
		var url = "/batchServiceRequests";
		var method = "POST";
		
		if( type == "수정" ){
			url = "/batchServiceRequests/"+batchServiceRequestSequencePk;
			method = "PATCH";
		}
		
		if( "batch" == modalType ){ // 배치 수정
			url = "/batchServices/"+batchServiceSequencePk;
			method = "PATCH";
			data["batchServiceSequencePk"] = batchServiceSequencePk;
			data["modelSequenceFk4"] = modelSequenceFk1;
			data["sandboxInstanceSequenceFk1"] = instanceSequenceFk2;
			data["projectSequenceFk3"] = projectSequenceFk3;
			data["enrollmentTerm"] = $("#userRequestTerm").val();
		}

		if( confirm(type+" 하시겠습니까?") ){
			var response = fnbatchServiceRequestsByAjax(url, method, data);
			if( response.result == "success" ){
				if( "batch" == modalType ){ // 배치 수정
					fnUpdateBatchRequestTable(response.batchService, type, modalType);/*업데이트 배치목록 테이블*/
				}else{
					fnUpdateBatchRequestTable(response.batchServiceRequest, type, modalType);/*업데이트 배치신청목록 테이블*/	
				}
				
				fnUnCheckbox();
				fnComNotify("success", "배치신청을  "+type+"하였습니다.");
				fnN2MCloseModal("#batchModal");
				
			}else if( response.result == "fail" && response.detail == "duplicateName" ){
				$("#name").focus();
				fnComNotify("warning","배치명이 중복되었습니다.");
				
			}else{
				fnComErrorMessage("배치 "+type+" 에러!!", response.detail);
			}
			
		}
	}
}

/*업데이트 배치신청목록 테이블*/
var fnUpdateBatchRequestTable = function(data, type, modalType){
	var batchCheckbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_SEQUENCE_PK+"'></label></div>";
	var batchRequestCheckbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'><label for='"+data.BATCH_SERVICE_REQUEST_SEQUENCE_PK+"'></label></div>";
    var resultUpdateMethod = "UPDATE";
    var name = "<a class='js-modal-show' href='#batchModal' onclick='N2M.ui.toggleModal(\"#batchModal\")'>"+data.NAME+"</a>";
    if( data.RESULT_UPDATE_METHOD == "replace" ) resultUpdateMethod = "REPLACE";

    var useFlag = "정지";
	if( data.USE_FLAG == "true" )		useFlag = "시작완료";
	
	var batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>에러</div>";
	if( data.BATCH_STATE == "success" )		batchState = "<div class='batchState' data-batchState="+data.BATCH_STATE+" data-pk='"+data.BATCH_SERVICE_SEQUENCE_PK+"'>성공</div>";
    
    if( "batch" == modalType ){
    	$("#logTable_batchList").dataTable().fnUpdate([
    		batchCheckbox, data.BATCH_SERVICE_SEQUENCE_PK, name, data.projectName, data.modelName
    		, data.NIFI_TEMPLATE_NAME, data.RESULT_UPDATE_DOMAIN_NAME, resultUpdateMethod, data.EXECUTION_CYCLE
			,useFlag ,batchState , data.createDataTime
		], clickRow);
    	
    }else if( type == "신청" ){
		var num = $("#logTable_batchRequestList").DataTable().rows().count()+1;
		$("#logTable_batchRequestList").dataTable().fnAddData([
			batchRequestCheckbox, data.BATCH_SERVICE_REQUEST_SEQUENCE_PK, name, data.projectName, data.modelName
			, data.NIFI_TEMPLATE_NAME, data.modelName, data.RESULT_UPDATE_DOMAIN_NAME, resultUpdateMethod, data.EXECUTION_CYCLE
			, data.createDataTime ,fnConvertProgressState(data.PROGRESS_STATE)
		]);
		$("#logTable_batchRequestList").DataTable().order([1, "desc"]).draw();
		
	}else{
		$("#logTable_batchRequestList").dataTable().fnUpdate([
			batchRequestCheckbox, data.BATCH_SERVICE_REQUEST_SEQUENCE_PK, name, data.projectName, data.modelName
			, data.NIFI_TEMPLATE_NAME, data.modelName, data.RESULT_UPDATE_DOMAIN_NAME, resultUpdateMethod, data.EXECUTION_CYCLE
			, data.createDataTime ,fnConvertProgressState(data.PROGRESS_STATE)
		], clickRow);
	}
}

/*배치신청 삭제*/
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
					fnUnCheckbox();
					fnComNotify("success", "배치신청을 삭제하였습니다.");
					successFlug = true;
				}else{
					fnComErrorMessage("배치신청 삭제 에러!!", response.detail);
				}
			}
			/* 테이블 삭제 */
			if( successFlug )	fnComDeleteTable("logTable_batchRequestList", checkRowList);
		}
		
	}else{
		fnComNotify("warning", "삭제할 목록을 선택해주세요.");
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
		if( confirm("배치를 삭제 하시겠습니까?") ){
			for( var i in checkIdList ){
				var response = fnDeleteBatchByAjax(checkIdList[i]);
				if( response.result == "success" ){
					fnUnCheckbox();
					fnComNotify("success", "배치을 삭제하였습니다.");
					successFlug = true;
				}else{
					fnComErrorMessage("배치 삭제 에러!!", response.detail);
				}
			}
			/* 테이블 삭제 */
			if( successFlug )	fnComDeleteTable("logTable_batchList", checkRowList);
		}
		
	}else{
		fnComNotify("warning", "삭제할 목록을 선택해주세요.");
	}
}

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
					};
				var response = fnStartAStopBatchByAjax(data);
				if( response.result == "success" ){
					fnComNotify("success", "배치를 "+comment+"하였습니다.");
					successFlug = true;
					/* 테이블 변경 */
					clickRow = checkRowList[i];
					fnUpdateBatchRequestTable(response.batchService, null, "batch");
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
	  				"targets": 0
	  				,"visible": false
	  			}
	  			,{
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
	  					return "<a class='js-modal-show button button__danger' href='#batchLogModal' onclick='N2M.ui.toggleModal(\"#batchLogModal\")'>상세</a>";
	  				}
	  			}
	  		]
	  } );
	  
	  $('.dataTables_filter').hide();
	  $('.dataTables_length').hide();
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
}
