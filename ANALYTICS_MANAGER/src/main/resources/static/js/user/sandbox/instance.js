var userRole = $("#userRole").val();
$("#loading").show();
$(function(){
	fnInit();
	
	/* 10초 주기로 반복 조회 후 상태값 변경 */
	setInterval(function(){
		fnChangeState();
	},30000);

	
	/* 인스턴스 생성용 서버성능 선택 클릭시*/
	$(document).on("click", "#serverList li", function(){
		var choosed = $(this);
		var serverId = choosed.attr("id");
		
		if( !choosed.hasClass("activeSel") ){
			$("#serverList").find("li").each(function(){
				$(this).removeClass("activeSel");
			});
			$(this).addClass("activeSel");

			fnSetInfoModal(serverId,"createInstance");
		}
	});
	
	/* 인스턴스 생성용  템플릿 목록 선택*/
	$(document).on("click", "#instanceTemplateList li", function(){
		var choosed = $(this);
		var templateId = choosed.attr("id");
		if( !choosed.hasClass("activeSel") ){
			$("#instanceTemplateList").find("li").each(function(){
				$(this).removeClass("activeSel");
			});
			$(this).addClass("activeSel");

			fnGetTemplateInfo(templateId,"createInstance");
		}
	});
	

})

var createTable = function(){
	  // datepicker
	  $("#requestCustomStartDate, #requestCustomEndDate").datepicker();
	  $("#startDate, #endDate").datepicker();
	  $("#u_startDate, #u_endDate").datepicker();
	  
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
		var beforeBtn	= '<a class="data-unit__button data-unit__button--like-add material-icons js-modal-show" href=\'#templateAdmin_modal\' type="button" onClick="fnTemplateManage()">템플릿 관리</a>';
		beforeBtn		+= '<a class="data-unit__button data-unit__button--request material-icons js-modal-show" href=\'#addSandBox_modal\' type="button" onClick="fnSandboxManage()">샌드박스 추가</a>';
		
		//$().append(beforeBtn);
		$(beforeBtn).prependTo(".dataTables_filter_custom");

		//테이블 상단 버튼 영역
		var addBtn	= "<button class='button material-icons play_arrow' onclick='fnStartInstance(\"sandbox\")'></button>";
		addBtn		+= "<button class='button material-icons stop' onclick='fnStopInstance(\"sandbox\")'></button>";
		addBtn		+= "<button class='button material-icons delete' onclick='fnDeleteInstance(\"sandbox\")'></button>";
		$(".dataTables_filter").append(addBtn);
		
		$('#logTable').DataTable().columns([1]).visible(false);
}


var fnInit = function(){
	if( userRole == "Analytics_Admin" ){
		$(".adminView").show();
		$(".userView").hide();
	}else{
		$(".adminView").hide();
		$(".userView").show();
	}
	fnSearch();
	$("#requestCustomTemplate").hide();
	$("#createTemplate").hide();
	$("#updateTemplate").hide();
}


/*인스턴스 목록 가져오기*/
var fnSearch = function(){
	var tbodyHtml = fnCreateListHtml(fnGetInstanceListByAjax());
	$("#tbodyHtml").html(tbodyHtml);
	$("#loading").hide();
}

/*인스턴스 목록 생성*/
var fnCreateListHtml = function(instances){
	var html = "";
	$("#sandboxTotalCnt").text(instances.length);
	for( var i in instances ){
		var data = instances[i];
		html += "";
		html += "<tr>";
		html += "	<td><div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.INSTANCE_SEQUENCE_PK+"'><label for='"+data.INSTANCE_SEQUENCE_PK+"'></label></div></td>";
		html += "	<td>"+data.INSTANCE_SEQUENCE_PK+"</td>";
		html += "	<td scope='row' title="+data.NAME+"><a class='js-modal-show' href='#templateInfo_modal' onclick='fnSetInfoModal(\""+data.CLOUD_INSTNACE_SERVER_ID+"\")'>"+data.NAME+"</a></td>";
		html += "	<td title="+data.TEMPLATE_NAME+">"+data.TEMPLATE_NAME+"</td>";
		if( data.SERVER_STATE.indexOf('call') > -1 ){
			html += "	<td><div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
					"			<div class='progress' style='margin-bottom:0px;'>" +
					"				<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>"+convertServerState(data.SERVER_STATE)+"중</div></div></td>";
		}else{
			html += "	<td><div class='serverState' data-serverState="+data.SERVER_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertServerState(data.SERVER_STATE)+"</div></td>";
		}
		
		if( data.MODULE_STATE == "checking" ){
			html += "	<td><div class='moduleState' data-serverState="+data.MODULE_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>" +
			"			<div class='progress' style='margin-bottom:0px;'>" +
			"				<div class='progress-bar progress-bar-striped active serverState' role='progressbar' style='width:100%'>체크중</div></div></td>";
			
		}else{
			html += "	<td><div class='moduleState' data-serverState="+data.MODULE_STATE+" data-pk='"+data.INSTANCE_SEQUENCE_PK+"'>"+convertModuleState(data.MODULE_STATE)+"</div></td>";			
		}
		
		html += "	<td>"+data.AVAILABILITY_ZONE+"</td>";
		html += "<td><a class='js-modal-show button button__danger' href='#detail_modal' onClick='fnNewPage(\""+data.INSTANCE_SEQUENCE_PK+"\",\"Hue\")'>Hue</a></td>";
		html += "<td><a class='js-modal-show button button__danger' href='#detail_modal' onClick='fnNewPage(\""+data.INSTANCE_SEQUENCE_PK+"\",\"Nifi\")'>Nifi</a></td>";
		html += "	<td>"+data.createDataTime+"</td>";
		if( userRole == "Analytics_Admin" )	html += "	<td>"+data.USER_ID+"</td>";
		html += "</tr>";
	}
	return html;
}



/*인스턴스 서버사양 가져오기*/
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
	
	if( option == undefined )	N2M.ui.toggleModal('#templateInfo_modal')
}


/*인스턴스 생성용 생성*/
var fnSandboxManage = function(){
	$("#instanceName").val("");
	
	// 서버 종류 가져오기
	fnGetServerList();
	
	// 사용가능한 템플릿 목록 가져오기
	fnGetTemplateList("createInstance");
	
	N2M.ui.toggleModal('#addSandBox_modal');
}

/*인스턴스 생성용 서버 종류 가져오기*/
var fnGetServerList = function(){
	var flavors = fnGetServerListByAjax();
	var html = "";

	for( var i in flavors ){
		var data = flavors[i];
		if( "m1.xxlarge" == data.name ){
			html += "<li class='activeSel' id='"+data.id+"'><a role='button'>"+data.name+"</a></li>";
			// 서버 정보 가져오기
			fnSetInfoModal(data.id,"createInstance");
		}
	}
	if( html == "" ) html = "서버목록이 없습니다.";
	$("#serverList").html(html);
}


/*인스턴스 생성용 템플릿 HTML*/
var fnCreateInstanceTemplateHtml = function(dataList){
	var html = "";
	for( var i in dataList ){
		var data = dataList[i];
		if( html == "" ){
			html += "<li class='activeSel' id="+data.ANALYSIS_TEMPLATE_SEQUENCE_PK+"><a role='button'>"+data.NAME+"</a></li>";
			fnCreateInstanceTemplateInfo(data);
		}else{
			html += "<li id="+data.ANALYSIS_TEMPLATE_SEQUENCE_PK+"><a role='button'>"+data.NAME+"</a></li>";
		}
	}
		
	$("#instanceTemplateList").html(html);
}

/*인스턴스 생성용 템플릿 정보 생성*/
var fnCreateInstanceTemplateInfo = function(data){
	var templateDataSummaryHtml = "";
	var instanceDataSummaryHtml = "";
	var templatePeriod = "   "+data.DATA_STARTDATE+" ~ "+data.DATA_ENDDATE;
	
	var templateDataSummary = data.DATA_SUMMARY.availableList;
	for( var i in templateDataSummary ){
		var num = Number(1)+Number(i);
		templateDataSummaryHtml += "<li>&nbsp;&nbsp;"+num+". "+templateDataSummary[i].name+"</li>";
	}
	
	var instanceDataSummary = data.DATA_SUMMARY.availableDetail;
	for( var i in instanceDataSummary ){
		var list = instanceDataSummary[i];
		for( var j in list ){
			instanceDataSummaryHtml += "<li>"+list[j].name+"</li>";
		}
	}
	
	$("#instanceTemplateDataSummary").html(templateDataSummaryHtml);
	$("#instanceInstanceDataSummary").html(instanceDataSummaryHtml);
	$("#instanceTemplatePeriod").text(templatePeriod);
}

/*인스턴스 생성*/
var fnCreateInstance = function(){
	if( $("#instanceTemplateList li").length == 0 ){
		fnComNotify("warning", "분석용 템플릿이 없습니다. 템플릿을 신청하세요.");
		return false;
		
	}else if( $.trim($("#instanceName").val()) == "" ){
		fnComNotify("warning", "인스턴스명을 입력해주세요.");
		$("#instanceName").focus();
		return false;
		
	}else{
		if( confirm("샌드박스를 생성하시겠습니까?") ){
			var data = {};
			// 인스턴스 생성시 필요한것
			// name
			data["name"] = $("#instanceName").val();

			// 사양 ID
			var cloudInstanceServerId = "";
			$("#serverList").find("li").each(function(){
				if( $(this).hasClass("activeSel") ){
					cloudInstanceServerId = $(this).attr("id");
				}
			});
			data["cloudInstanceServerId"] = cloudInstanceServerId;
			
			// 템플릿 ID snapshotId(템플릿)
			var templateId = "";
			$("#instanceTemplateList").find("li").each(function(){
				if( $(this).hasClass("activeSel") ){
					templateId = $(this).attr("id");
				}
			});
			data["templateId"] = templateId;
			
			/*properties (키페어 Name, 가용구역 Name, 네트워크 ID, 보안그룹 ID)*/
			var response = fnCreateInstanceByAjax(data);
			if( response.result == "success" ){
				fnUpdateTable(response.instance);
				fnN2MCloseModal("#addSandBox_modal");
				$("#sandboxTotalCnt").text($("#logTable >tbody tr").length);
				fnComNotify("success", "샌드박스를 생성하였습니다.");
				
			}else if( response.detail == "duplicateName"){
				$("#instanceName").focus();
				fnComNotify("warning","샌드박스명이 중복되었습니다.");
			
			}else if( response.detail == "disk is smaller than the minimum"){
				fnComNotify("warning","디스크가 스냅샷이미지보다 작습니다.");
			
			}else if( response.detail == "Quota exceeded for ram:"){
				fnComNotify("warning","샌드박스 허용 메모리를 초과하였습니다.");
				
			}else if( response.detail == "Quota exceeded for cores:"){
				fnComNotify("warning","샌드박스 허용 CPU를 초과하였습니다.");
				
			}else{
				fnComErrorMessage(JSON.stringify(response.detail.detail).replace(/\"/gi,""), response.detail);
			}
		}
	}
}

/*업데이트 data*/
var fnUpdateTable = function(data){
	var checkbox = "<div class='checkboxCustom'><input type='checkbox' name='table_records' id='"+data.INSTANCE_SEQUENCE_PK+"'><label for='"+data.INSTANCE_SEQUENCE_PK+"'></label></div>";
	var name = "<a class='js-modal-show' href='#templateInfo_modal' onclick='fnSetInfoModal(\""+data.CLOUD_INSTNACE_SERVER_ID+"\")'>"+data.NAME+"</a>";
	var serverState = data.SERVER_STATE;
	var moduleState = data.MODULE_STATE;
	var nifi = "<a class='js-modal-show button button__danger' href='#detail_modal' onClick='fnNewPage(\""+data.INSTANCE_SEQUENCE_PK+"\",\"Nifi\")'>Nifi</a>";
	var hue = "<a class='js-modal-show button button__danger' href='#detail_modal' onClick='fnNewPage(\""+data.INSTANCE_SEQUENCE_PK+"\",\"Hue\")'>Hue</a>";
	
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
	
	var num = $("#datatable-checkbox").DataTable().rows().count()+1;
	
	if( userRole == "Analytics_Admin" ){
		$("#logTable").dataTable().fnAddData([
			checkbox, data.INSTANCE_SEQUENCE_PK, name, data.TEMPLATE_NAME, serverState, moduleState
			/*, privateIp*/, data.AVAILABILITY_ZONE, hue, nifi, data.createDataTime, data.USER_ID
		]);	
	}else{
		$("#logTable").dataTable().fnAddData([
			checkbox, data.INSTANCE_SEQUENCE_PK, name, data.TEMPLATE_NAME, serverState, moduleState
			/*, privateIp*/, data.AVAILABILITY_ZONE, hue, nifi, data.createDataTime
		]);
	}
	
	$("#logTable").DataTable().order([1, "desc"]).draw();
}

/*인스턴스 목록의 상태값 갱신*/
var fnChangeState = function(){
	/*인스턴스 목록 가져요기*/
	var instances = fnGetInstanceListByAjax();
	for( var i in instances ){
		var data = instances[i];
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

/*Nifi, Hue 새창*/
var fnNewPage = function(instanceSequencePk, type){
	var url = fnSandboxSetInstancePkInSessionByAjax(instanceSequencePk);

	if( type == "Nifi" )	url = url+"/nifi/";
	else	url = url+"/hue/";

	window.open(url);
}















