var modelIntervalId = 0;
$(function(){
	
	// 모델 리스트 클릭시
	$(document).on("click", ".modelData", function(){
		/*파일브라우저 샘플 미리보기*/
		$(".modelData").removeClass("activeSel");
		$(this).addClass("activeSel");
		selectedModelPk = $(this).attr("id");
		fnGetModel();
	});
	
	// 모델테스트 로컬파일 클릭시
	$(document).on("click", ".modelLocalFiles", function(){
		/*파일브라우저 샘플 미리보기*/
		$(".modelLocalFiles").parent().removeClass("active");
		$(this).parent().addClass("active");
		fnGetModelLocalFileSample($(this).text());
	});
	
	// 배치등록 버튼 클릭시
	$(document).on("click", "#batchModalBtn", function(){
		$("#batchRequestForm").find("input").each(function(){
			$(this).val("");
		});
		$("#userRequestTerm").val("");
		$(".modalName").text("등록");
		$("#selectedModel").val($(".modelName").first().text());
		// 도메인명 가져오기(템플릿 허용 데이터 가져오기)
		fnGetRequestTemplateAvailable();
	});
});

/*모델 목록 가져오기*/
var fnGetModelList = function(){
	var html = "";
	if( fnNotNullAndEmpty(selectedPreprocessedDataPk) ){
		var modelList = fnGetModelsByAjax(projectSequencePk, selectedPreprocessedDataPk);
		for( var i in modelList ){
			var model = modelList[i];
			if( i == 0 ){
				html += "<li class='modelData activeSel' id="+model.MODEL_SEQUENCE_PK+"><a href='javascript:'>"+model.NAME+"</a></li>";
				
				selectedModelPk = model.MODEL_SEQUENCE_PK;
				fnGetModel();/* 전처리데이터 가져오기*/
				
			}else{
				html += "<li class='modelData' id="+model.MODEL_SEQUENCE_PK+"><a href='javascript:'>"+model.NAME+"</a></li>";
			}
		}
	}
	
	if( html == "" ){
		html += "<li>모델 데이터가 없습니다.</li>";
		$("#modelDiv").hide();		  // 모델 숨김
		
		// 종료
		if( modelIntervalId != 0 )	clearInterval(modelIntervalId);
		
	}else{
		$("#deleteModelBtn").show();
		$("#modelDiv").fadeIn();		// 모델 숨김
	}
	
	$("#modelList").html(html);
	$("#loading").hide();
}

/*모델 상세내용*/
var fnGetModel = function(model){
	if( model == null ){
		model = fnGetModeslByAjax(projectSequencePk, selectedModelPk);
	}
	$(".modelName").text(model.NAME);
	
	$("#modelDetailDiv").hide();
	$("#modelTestBtn").hide();
	$("#batchModalBtn").hide();
	if( modelIntervalId != 0 )	clearInterval(modelIntervalId);
	
	/* 전처리 상태값 체크 후 변경 */
	if( "ongoing" == model.PROGRESS_STATE ){
		var html = "";
		html += "	<div class='progress' style='margin-bottom:0px;'>" +
		"				<div class='progress-bar progress-bar-striped active' role='progressbar' style='width:100%'>생성중</div></div>";
		
		var stopHtml = "<button class='button button__danger' onclick='fnStopModel();'>중지</button>";
		$("#modelFilename").html(html);
		$("#modelFilepath").html(html);
		$("#modelCreateDatetime").text(model.CREATE_DATETIME.substring(0,19).replace("T"," "));
		$("#progressState").html(stopHtml);
		modelIntervalId = setInterval(fnChangeModelState, 5000);
		
	}else if( "standby" == model.PROGRESS_STATE ){
		var restartHtml = "<button class='button button__danger' onclick='fnRestartModel("+model.MODEL_SEQUENCE_PK+");'>재시작</button>";
		$("#modelFilename").text("중지");
		$("#modelFilepath").text("중지");
		$("#modelCreateDatetime").text(model.CREATE_DATETIME.substring(0,19).replace("T"," "));
		$("#progressState").html(restartHtml);
		
	}else if( "fail" == model.PROGRESS_STATE ){
		$("#modelFilename").text("실패");
		$("#modelFilepath").text("실패");
		$("#modelCreateDatetime").text("실패");
		$("#progressState").text("실패");
		
	}else{ // 성공
		$("#modelFilename").text(model.FILENAME);
		$("#modelFilepath").text(model.FILEPATH);
		$("#modelCreateDatetime").text(model.CREATE_DATETIME.substring(0,19).replace("T"," "));
		$("#progressState").text("학습완료");
		$("#modelDetailDiv").fadeIn();
		$("#modelTestBtn").show();
		if( userRole == "general" )	$("#batchModalBtn").show();
		
		// 모델 학습정보
		fnSetModelInfo(model);
	}
	
	$("#modelDiv").fadeIn();
}

/*모델 학습정보*/
var fnSetModelInfo = function(model){
	var command = model.COMMAND;
	var algorithm = fnAlgorithmByAjax(command.algorithms_sequence_pk);

	var html = "";
	html += "<tr><th>라이브러리</th><td colspan='2'>"+algorithm.LIBRARY_NAME+"</td></tr>";
	html += "<tr><th>알고리즘</th><td colspan='2'>"+algorithm.LIBRARY_FUNCTION_NAME+"</td></tr>";
	html += "<tr><th>학습 소요 시간</th><td colspan='2'>"+model.diffDateTime+"</td></tr>";
	html += "<tr><th>모델 성능 평가</th><td colspan='2'>"+model.VALIDATION_SUMMARY.holdout_score+"</td></tr>";
	
	var modelParam = command.model_parameters;
	var trainParam = command.train_parameters;
	var cnt = 0;
	$.each(modelParam, function(key, value){
		if( cnt == 0 )	html += "<tr><th rowspan='"+Object.keys(modelParam).length+"'>모델 파라미터</th><td>"+key+"</td><td>"+value+"</td></tr>";
		else 			html += "<tr><td>"+key+"</td><td>"+value+"</td></tr>";
		cnt++;
	});
	cnt = 0;
	$.each(trainParam, function(key, value){
		if( cnt == 0 )	html += "<tr><th rowspan='"+Object.keys(trainParam).length+"'>학습 파라미터</th><td>"+key+"</td><td>"+fnArraySplitBR(value)+"</td></tr>";
		else 			html += "<tr><td>"+key+"</td><td>"+fnArraySplitBR(value)+"</td></tr>";
		cnt++;
	});

	$("#modelInfo").html(html);
}


/*모델 삭제*/
var fnDeleteModel = function(){
	if( confirm("모델을 삭제하시면 배치관련 데이터도 같이 삭제됩니다. \n삭제하시겠습니까?") ){
		var result = fnDeleteModelByAjax(projectSequencePk, selectedModelPk);
		if( result == "success" ){
			fnComNotify("success", "모델을 삭제 하였습니다.");
			// 모델 목록 가져오기
			fnGetModelList();
		}
	}
}

/*모델 중지*/
var fnStopModel = function(){
	if( confirm("학습을 중지 요청을 하시겠습니까?") ){
		var data = {
				"mode" : "STOP"
		}
		var result = fnStopAndRestartModelByAjax(projectSequencePk, selectedModelPk, data);
		if( result == "success" ){
			fnComNotify("success", "모델 생성중지 요청을 하였습니다.");
		}		
	}
}

/*모델 재시작*/
var fnRestartModel = function(){
	if( confirm("학습을 재시작 요청을 하시겠습니까?") ){
		var data = {
				"mode" : "RESTART"
		}
		var result = fnStopAndRestartModelByAjax(projectSequencePk, selectedModelPk, data);
		if( result == "success" ){
			fnComNotify("success", "모델 재시작 요청을 하였습니다.");
			// 모델 목록 가져오기
			fnGetModelList();
		}		
	}
}

/*모델 상태변경*/
var fnChangeModelState = function(){
	var model = fnGetModeslByAjax(projectSequencePk, selectedModelPk);
	
	if( "success" == model.PROGRESS_STATE ){
		$("#modelFilename").text(model.FILENAME);
		$("#modelFilepath").text(model.FILEPATH);
		$("#modelCreateDatetime").text(model.CREATE_DATETIME.substring(0,19).replace("T"," "));
		$("#progressState").text("학습완료");
		$("#modelDetailDiv").fadeIn();
		$("#modelTestBtn").show();
		if( userRole == "general" )	$("#batchModalBtn").show();
		
		// 모델 학습정보
		fnSetModelInfo(model);
		
		// 종료
		if( modelIntervalId != 0 )	clearInterval(modelIntervalId);
		
	}else if( "standby" == model.PROGRESS_STATE ){
		var reStartHtml = "<button class='button button__danger' onclick='fnRestartModel();'>재시작</button>";
		$("#modelFilename").html("<td>중지</td>");
		$("#modelFilepath").html("<td>중지</td>");
		$("#progressState").html(reStartHtml);
		$("#modelCreateDatetime").text(model.CREATE_DATETIME.substring(0,19).replace("T"," "));
		
		if( modelIntervalId != 0 )	clearInterval(modelIntervalId);
		
	}else if( "fail" == model.PROGRESS_STATE ){
		$("#modelFilename").html("<td>실패</td>");
		$("#modelFilepath").html("<td>실패</td>");
		$("#modelCreateDatetime").html("<td>실패</td>");
		$("#progressState").html("<td>실패</td>");
		
		fnComNotify("error", "모델 생성을 실패 하였습니다. 관리자에게 문의해주세요.");
		if( modelIntervalId != 0 )	clearInterval(modelIntervalId);
	}
}

/*모델 테스트 모달*/
var fnModelTestModal = function(){
	var response = fnGetSandboxFileBrowserByAjax(selectedInstancePk);

	if( response.result == "success" ){
		var localFiles = response.localFiles;
		var html = "";
		for( var i in localFiles ){
			if( i == 0 ){
				fnGetModelLocalFileSample(localFiles[i]);
				html += "<li class='active' role='button' title='"+localFiles[i]+"'><a href='javascript:' class='modelLocalFiles'>"+localFiles[i]+"</a></li>";
			}else{
				html += "<li  role='button' title='"+localFiles[i]+"'><a href='javascript:' class='modelLocalFiles'>"+localFiles[i]+"</a></li>";
			}
		}
		$("#modelLocalFiles").html(html);
		fnOpenModal("modelTestModal");

	}else if( response.title == "Resource Not Found" ){
		fnComNotify("warning","샌드박스가 존재하지 않습니다.");

	}else if( response.title == "Operation Not Supported" ){
		fnComNotify("warning","샌드박스가 꺼져있습니다.");
	}
}

/*모델 테스트 로컬파일 샘플*/
var fnGetModelLocalFileSample = function(localFile){
	var localFile = fnGetLocalFileSampleByAjax(selectedInstancePk, localFile);
	$("#modelLocalFileSample").html("<pre>"+JSON.stringify(localFile,null,2)+"</pre>");
	$("#modelTestResult").text("");
}

/*모델 테스트*/
var fnModelTest = function(){
	$("#loading").show();
	var localFile = "";
	var data = {};
	$(".modelLocalFiles").each(function(){
		if( $(this).parent().hasClass("active") ){
			localFile = $(this).text();
		}
	});
	data["mode"] = "TEST";
	data["test_data_path"] = localFile;
	
	var result = fnModelTestByAjax(projectSequencePk, selectedModelPk, data);
	$("#modelTestResult").html(JSON.stringify(result,null,2));
	$("#loading").hide();
}

/*도메인명 가져오기(템플릿 허용 데이터 가져오기)*/
var fnGetRequestTemplateAvailable = function(){
	var html = "";
	var availableList = fnGetRequestTemplateAvailableByAjax();
	for( var i in availableList ){
		html += "<option value="+availableList[i].id+">"+(availableList[i].name==""?availableList[i].id:availableList[i].name)+"</option>";
	}
	$("#resultUpdateDomain").html(html);
}


/*배치신청 등록*/
var fnSaveBatchRequest = function(){
	// validation
	if( $.trim($("#batchName").val()) == "" ){
		fnComNotify("warning", "배치명을 입력해주세요.");
		$("#batchName").focus();
		return false;
		
	}else if( $.trim($("#nifiTemplateName").val()) == "" ){
		fnComNotify("warning", "NIFI 템플릿명을 입력해주세요.");
		$("#nifiTemplateName").focus();
		return false;

	}else if( $.trim($("#totalColumnName").val()) == "" ){
		fnComNotify("warning", "전체값 컬럼이름을 입력해주세요.");
		$("#totalColumnName").focus();
		return false;

	}else if( $.trim($("#domainIdColumnName").val()) == "" ){
		fnComNotify("warning", "도메인컬럼 이름을 입력해주세요.");
		$("#domainIdColumnName").focus();
		return false;
	}else if( $.trim($("#updateAttribute").val()) == "" ){
		fnComNotify("warning", "업데이트하는 속성을 입력해주세요.");
		$("#updateAttribute").focus();
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
			if( i == 0 ) executionCycle = $("#executionCycle_"+i).val();
			else  executionCycle += " "+$("#executionCycle_"+i).val();
		}
		
		var data = {
			"name" : $("#batchName").val()
			,"modelSequenceFk1" : selectedModelPk
			,"instanceSequenceFk2" : selectedInstancePk
			,"projectSequenceFk3" : projectSequencePk
			,"nifiTemplateName" : $("#nifiTemplateName").val()
			,"resultUpdateDomainId" : $("#resultUpdateDomain").val()
			,"resultUpdateDomainName" : $("#resultUpdateDomain option:selected").text()
			,"executionCycle" : executionCycle
			,"resultUpdateMethod" : $("#resultUpdateMethod").val()
			,"userRequestTerm" : $("#userRequestTerm").val()
			,"storeMethod" : $("#storeMethod").val()
			,"isReverseIndex" : $("#isReverseIndex").val()
			,"totalColumnName" : $("#totalColumnName").val()
			,"domainIdColumnName" : $("#domainIdColumnName").val()
			,"updateAttribute" : $("#updateAttribute").val()
		};

		var url = "/batchServiceRequests"; 
		var method = "POST";
		if( confirm("배치를 신청 하시겠습니까?") ){
			var response = fnbatchServiceRequestsByAjax(url, method, data);
			if( response.result == "success" ){
				fnComNotify("success", "배치신청을  하였습니다.");
				fnCloseModal("batchModal");
				
			}else if( response.result == "fail" && response.detail == "duplicateName" ){
				$("#batchName").focus();
				fnComNotify("warning","배치명이 중복되었습니다.");
				
			}else{
				fnComErrorMessage("배치신청 에러!!", response.detail);
			}
			
		}
	}
}



