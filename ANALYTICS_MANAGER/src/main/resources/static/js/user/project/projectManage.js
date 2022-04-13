var clickRow;
var userRole = $("#userRole").val();
$("#loading").show();
$(function(){
	fnInit();
	
	/*프로젝트 등록/수정*/
	$(document).on("click", "#saveProjectBtn", function(){
		if( "등록" == $(this).text() )	fnSaveProject("regist");
		else fnSaveProject("update");
	});
	
	/* 테이블 클릭시 */
	$(document).on("click", "#tbodyHtml td", function(){
		var index = 5;
		if( "Analytics_Admin" == userRole ) index = 6;
		if( $(this).index() == index ){
			var clickRows = $("#logTable").dataTable().fnGetPosition(this); // 변경하고자 하는 clickRow
			clickRow = clickRows[0];

		}else if( $(this).index() == 1 ){
			var data = $("#logTable").dataTable().fnGetData($(this).parent());
			fnMovePageWithGet("/projectDetail", ["id"], [data[1]]);
		}
	});
});

var createTable = function(){
  // datepicker
  $("#logTable").DataTable( {
		// dom: "aatestestset",
		"language": {
			"emptyTable": "데이터가 없습니다.",
			"lengthMenu": "_MENU_",
			//"info": "_START_ - _END_ / _TOTAL_ 개",
			"info": "",
			"infoEmpty": "",
			"infoFiltered": "( _MAX_건의 데이터에서 필터링됨 )",
			"search": "",
			"searchPlaceholder": "검색어를 입력하세요",
			"zeroRecords": "일치하는 데이터가 없습니다.",
			"loadingRecords": "로딩중...",
			"processing":     "잠시만 기다려 주세요...",
			"paginate": {
			"next": "다음",
			"previous": "이전"
			}
		}
		,"autoWidth": false
  } );	//DataTable

	//테이블 상단 class 수정
	$('select[name="logTable_length"]').addClass("select");
	$('select[name="logTable_length"]').hide();
	$('.dataTables_filter').addClass("dataTables_filter_custom");
	$('.dataTables_length').addClass("dataTables_length_custom");
	$('.dataTables_filter_custom input[type="search"]').addClass("input input__search");

	//테이블 상단 버튼 영역 추가
	var beforeBtn	= '<a class="data-unit__button data-unit__button--request material-icons js-modal-show" href=\'#projectInfo_modal\' type="button" onClick="fnAddProjcetModal()">등록</a>';
	beforeBtn		+= '<a class="data-unit__button data-unit__button--request material-icons delete" type="button" onClick="fnDeleteProjcet()">삭제</a>';
	
	//$().append(beforeBtn);
	$(beforeBtn).prependTo(".dataTables_filter_custom");

	$('#logTable').DataTable().columns([1]).visible(false);
}

var fnInit = function(){
	fnSearch();
}

/*프로젝트 목록 조회*/
var fnSearch = function(){
	var projectList = fnGetProjectListByAjax();
	$("#tbodyHtml").html(fnCreateListHtml(projectList));
	$("#loading").hide();
}

/*프로젝트 목록 생성*/
var fnCreateListHtml = function(projects){
	var html = "";
	$("#projectTotalCnt").text(projects.length);
	for( var i in projects ){
		var data = projects[i];
		html += "";
		html += "<tr>";
		html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.PROJECT_SEQUENCE_PK+"'><label for='"+data.PROJECT_SEQUENCE_PK+"'></label></div></td>";
		html += "	<td>"+data.PROJECT_SEQUENCE_PK+"</td>";
		html += "	<td scope='row' class='pointerCorsor'>"+data.NAME+"</td>";
		html += "	<td>"+data.DESCRIPTION+"</td>";
		html += "	<td>"+data.createDataTime+"</td>";
		html += "	<td>"+data.instanceName+"</td>";
		if( userRole == "Analytics_Admin" )	html += "	<td>"+data.USER_ID+"</td>";
		html += "	<td><a class='js-modal-show button button__danger' href='#projectInfo_modal' onclick='fnUpdateModal(\""+data.PROJECT_SEQUENCE_PK+"\");'>수정</a></td>";
		html += "</tr>";
	}
	return html;
}

/*프로젝트 등록 모달*/
var fnAddProjcetModal = function(){
	fnGetInstanceList(); // 인스턴스 목록 가져오기
	if( fnNotNullAndEmpty($("#selectedInstance").val()) ){
		$("#modalTitle").text("프로젝트 등록");
		$("#saveProjectBtn").text("등록");
		$("#projectSequencePk").val("");
		$("#name").val("").focus();
		$("#description").val("");
		$(".registDiv").show();
		N2M.ui.toggleModal('#projectInfo_modal');	
	}else{
		fnComNotify("warning", "샌드박스를 생성해주세요.");
	}
}

/*프로젝트 수정 모달*/
var fnUpdateModal = function(projectSequencePk){
	$("#modalTitle").text("프로젝트 수정");
	$("#saveProjectBtn").text("수정");
	
	var project = fnGetProjectByAjax(projectSequencePk);
	$("#projectSequencePk").val(project.PROJECT_SEQUENCE_PK);
	$("#name").val(project.NAME);
	$("#description").val(project.DESCRIPTION);
	$(".registDiv").hide();
	N2M.ui.toggleModal('#projectInfo_modal');
}

/*인스턴스 목록 가져오기*/
var fnGetInstanceList = function(){
	var instanceList = fnGetInstanceListByAjax();
	var html = "";
	for( var i in instanceList ){
		html += "<option value='"+instanceList[i].INSTANCE_SEQUENCE_PK+"'>"+instanceList[i].NAME+"</option>";
	}
	$("#selectedInstance").html(html);
}


/*프로젝트 등록/수정*/
var fnSaveProject = function(option){
	if( $.trim($("#name").val()) == "" ){
		fnComNotify("warning", "프로젝트명을 입력해주세요.");
		$("#name").focus();
		return false;
		
	}else{
		var data = {
			"name" : $("#name").val()
			,"description" : $("#description").val()
		};
		
		var subject = "등록";
		var url = "/projects";
		var type = "POST";
		
		if( option == "regist" ){
			data["selectedInstance"] = $("#selectedInstance option:selected").val();
			
		}else if( "update" == option ){
			subject = "수정";
			url = url+"/"+$("#projectSequencePk").val();
			type = "PATCH";
			data["projectSequencePk"] = $("#projectSequencePk").val();
			if( data["description"] == "" ){
				data["description"] = " ";
			}
		}

		if( data != "" && confirm(subject+"하시겠습니까?")){
			var response = fnSaveProjectByAjax(url, type, data);

			if( response.result == "success" ){
				fnUpdateTable(response.project, option);
				fnUnCheckbox();
				fnComNotify("success", "프로젝트를  "+subject+"하였습니다.");
				fnN2MCloseModal("#projectInfo_modal");

				// edited start 2022.01.17 by PHW 전체 건수 증가 및 감소
				$("#projectTotalCnt").text($("#tbodyHtml tr").length);
				// edited end 2022.01.17 by PHW 전체 건수 증가 및 감소
			      
			}else if( response.detail == "duplicateName"){
				$("#name").focus();
				fnComNotify("warning","프로젝트명이 중복되었습니다.");
				
			}else{
				fnComErrorMessage("프로젝트 "+subject+" 에러!!", response.detail);
			}
		}
	}
}

/*업데이트 data*/
var fnUpdateTable = function(data, option){
	var checkbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.PROJECT_SEQUENCE_PK+"'><label for='"+data.PROJECT_SEQUENCE_PK+"'></label></div>";
    var modifyBtn = "<a class='js-modal-show button button__danger' href='#projectInfo_modal' onclick='fnUpdateModal(\""+data.PROJECT_SEQUENCE_PK+"\");'>수정</a>";
    
	if( option == "regist" ){
		var num = $("#logTable").DataTable().rows().count()+1;
		
		if( userRole == "Analytics_Admin" ){
			$("#logTable").dataTable().fnAddData([
				checkbox, data.PROJECT_SEQUENCE_PK, data.NAME, data.DESCRIPTION
				, data.createDataTime, data.instanceName, data.USER_ID, modifyBtn
			]);
		}else{
			$("#logTable").dataTable().fnAddData([
				checkbox, data.PROJECT_SEQUENCE_PK, data.NAME, data.DESCRIPTION
				, data.createDataTime, data.instanceName, modifyBtn
			]);
		}
		$("#logTable").DataTable().order([1, "desc"]).draw();
		// edited start 2022.01.17 by PHW 테이블 마우스 포인터 설정
		$("#logTable td:eq(1)").css({cursor:"pointer"});
		// edited end 2022.01.17 by PHW 테이블 마우스 포인터 설정
		
	}else{
		if( userRole == "Analytics_Admin" ){
			$("#logTable").dataTable().fnUpdate([
				checkbox, data.PROJECT_SEQUENCE_PK, data.NAME, data.DESCRIPTION
				, data.createDataTime, data.instanceName, data.USER_ID, modifyBtn
			], clickRow);
			
		}else{
			$("#logTable").dataTable().fnUpdate([
				checkbox, data.PROJECT_SEQUENCE_PK, data.NAME, data.DESCRIPTION
				, data.createDataTime, data.instanceName, modifyBtn
			], clickRow);
		}
	}
}


var fnDeleteProjcet = function(){
	// 체크된 항목 가져오기
	var checkMap = fnTableCheckList("tbodyHtml");
	var checkIdList = checkMap.checkIdList;
	var checkRowList = checkMap.checkRowList;
	var successFlug = false;
	if( checkIdList.length > 0 ){
		if( confirm("프로젝트 삭제시 관련데이터(원본데이터, 전처리, 모델)도 같이 삭제됩니다. \n삭제 하시겠습니까?") ){
			for( var i in checkIdList ){
				var response = fnDeleteProjcetByAjax(checkIdList[i]);
				if( response.result == "success" ){
					fnUnCheckbox();
					fnComNotify("success", "프로젝트를 삭제하였습니다.");
					successFlug = true;
				}else{
					fnComErrorMessage("프로젝트 삭제 에러!!", response.detail);
				}
			}
			/* 테이블 삭제 */
			if( successFlug )	fnComDeleteTable("logTable", checkRowList);

			// edited start 2022.01.17 by PHW 전체 건수 증가 및 감소
			$("#projectTotalCnt").text($("#tbodyHtml tr").length);
			// edited end 2022.01.17 by PHW 전체 건수 증가 및 감소
		}
		
	}else{
		fnComNotify("warning", "삭제할 목록을 선택해주세요.");
	}
}























