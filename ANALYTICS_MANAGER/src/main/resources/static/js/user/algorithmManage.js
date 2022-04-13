$("#loading").show();
$(function(){
	fnInit();
	
	/*상세버튼 클릭시*/
	$(document).on("click", "#seeDetailBtn", function(){
		
	});
})

var createTable = function(){
	  $("#logTable").DataTable( {
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
		$('.dataTables_filter input[type="search"]').addClass("input input__search");
		
}

var fnInit = function(){
	fnSearch();
}

var fnSearch = function(){
	var algorithms = fnAlgorithmListByAjax();
	$("#tbodyHtml").html(fnCreateListHtml(algorithms));
	$("#loading").hide();
}

var fnCreateListHtml = function(algorithms){
	var html = "";
	$("#totalCount").text(algorithms.length);
	for( var i in algorithms ){
		var data = algorithms[i];
		html += "";
		html += "<tr>";
		html += "	<td>"+data.LIBRARY_FUNCTION_USAGE+"</td>";
		html += "	<td>"+data.ALGORITHM_NAME+"</td>";
		html += "	<td>"+data.LIBRARY_NAME+"</td>";
		html += "	<td>"+data.SUPPORT_DATA_TYPE+"</td>";
		html += "<td><a class='js-modal-show button button__danger' href='#detail_modal' onClick='fnSetModal("+data.ALGORITHM_SEQUENCE_PK+")'>See detail</a></td>";
		html += "</tr>";
	}
	return html;
}

var fnSetModal = function(pk){
	var data = fnAlgorithmByAjax(pk);
	$("#algorithmName").text(fnReplaceNull(data.ALGORITHM_NAME));
	$("#libraryName").text(fnReplaceNull(data.LIBRARY_NAME));
	$("#libraryFunctionUasge").text(fnReplaceNull(data.LIBRARY_FUNCTION_USAGE));
	$("#libraryFunctionDescription").text(fnReplaceNull(data.LIBRARY_FUNCTION_DESCRIPTION));
	$("#modelParamList").html(fnCreateModalHtml(data.MODEL_PARAMETERS));
	$("#trainParamList").html(fnCreateModalHtml(data.TRAIN_PARAMETERS));
	
	if( fnNotNullAndEmpty(data.MODEL_PARAMETERS) && fnNotNullAndEmpty(data.MODEL_PARAMETERS) ){
		var jsonParameter = {};
		jsonParameter["MODEL_PARAMETERS"] = data.MODEL_PARAMETERS;
		jsonParameter["TRAIN_PARAMETERS"] = data.TRAIN_PARAMETERS;
		$("#jsonParameter").html("<pre>"+JSON.stringify(jsonParameter,null,2)+"</pre>");
	}
	N2M.ui.toggleModal('#templateInfo_modal')
}

var fnCreateModalHtml = function(list){
	var html = "";
	for( var i in list ){
		var data = list[i];
		html += "";
		html += "<tr>";
		html += "	<td>"+data.name+"</td>";
		html += "	<td>"+data.default+"</td>";
		html += "	<td>"+data.type+"</td>";
		html += "	<td>"+data.note+"</td>";
		html += "</tr>";
	}
	return html;
}
