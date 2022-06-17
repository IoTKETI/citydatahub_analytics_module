$(function(){
	
	var urlName = window.location.pathname;
	createTable();
	
	$(".commonMenuUl").find("li").each(function(){
		$(this).removeClass("is-active");
	});
	
	if( urlName == "/algorithmManage" )		$(".algorithmActive").addClass("is-active");
	else if( urlName == "/projectManage" || urlName == "/projectDetail")	$(".projectManageActive").addClass("is-active");
	else if( urlName == "/batchManage" )		$(".batchManageActive").addClass("is-active");
	
	// a href="#" 적용 안되게 막기
	$(document).on('click', 'a[href="#"]', function(e){ e.preventDefault(); });
	
	//모달창 닫기
	$(".js-modal-close").click(function(){
		$("html").removeClass("is-scroll-blocking");
	});

	/*체크박스의 모두체크버튼*/
	$("#check-all_batchList").click(function(){
		var chkFlug = false;
		if( $(this).is(":checked") )	chkFlug = true;
		$("input[name='table_records']").each(function(){
			$(this).prop("checked", chkFlug);
		});
	});
	$("#check-all_batchRequestList").click(function(){
		var chkFlug = false;
		if( $(this).is(":checked") )	chkFlug = true;
		$("input[name='table_records']").each(function(){
			$(this).prop("checked", chkFlug);
		});
	});
	$("#check-all").click(function(){
		var chkFlug = false;
		if( $(this).is(":checked") )	chkFlug = true;
		$("input[name='table_records']").each(function(){
			$(this).prop("checked", chkFlug);
		});
	});
});

/*********************************************** Common function start ***********************************************/

/* 
 * 동기화 ajax로 데이터 가져오기/보내기
 * url(필수) : url
 * errorMessage : 에러시 메시지
 * fn_success : 성공시 호출 될 함수
 * fnAjaxPostData
 */
var fnAjaxGetDataSync = function(url, errorMessage, fn_success ) {
	$.ajax({
		url: url,
		type: "GET",
		async: false,
		success: function(response) {
			if (fn_success)	fn_success(response);
		},
		error: function(response){
			fnComErrorMessage(errorMessage+"(statusCode:"+response.status+") \n"+response.responseJSON.detail, response.responseJSON.detail);
		}
	});
};

/* 
 * 동기화 ajax로 데이터 가져오기/보내기
 * url(필수) : url
 * req_data : 파라메터. json 타입"
 * errorMessage : 에러시 메시지
 * fn_success : 성공시 호출 될 함수
 * fnAjaxPostData
 */
var fnAjaxDataSync = function(url, type, req_data, errorMessage, fn_success ) {
	$.ajax({
		url: url,
		type: type,
		dataType: "json",
		contentType: 'application/json',
		data: req_data,
		async: false,
		success: function(response) {
			if (fn_success)	fn_success(response);
		},
		error: function(response){
			fnComErrorMessage(errorMessage+"(statusCode:"+response.status+") \n"+response.responseJSON.detail, response.responseJSON.detail);
		}
	});
};

var fnAjaxDeleteDataSync = function(url, errorMessage, fn_success ) {
	$.ajax({
		url: url,
		type: "DELETE",
		contentType: 'application/json',
		async: false,
		success: function(response) {
			if (fn_success)	fn_success(response);
		},
		error: function(response){
			fnComErrorMessage(errorMessage+"(statusCode:"+response.status+") \n"+response.responseJSON.detail, response.responseJSON.detail);
		}
	});
};


function fnSetDatepicker(startId, endId){
	$("#"+startId).datepicker({
	    language:  'ko'
		,weekStart: 1
	    ,format: "yyyy-mm-dd"
	});

	$("#"+endId).datepicker({
	    language:  'ko'
	    ,weekStart: 1
	    ,format: "yyyy-mm-dd"
	});
	
	$("#"+startId).val(fnGetDate("lastMonth").searchStartDate);
	$("#"+endId).val(getFormatDate(new Date()));
}




/*********************************************** 페이징 처리 ***********************************************/
/*
divId : 페이징 태그가 그려질 div
pageIndx : 현재 페이지 위치가 저장될 input 태그 id
recordCount : 페이지당 레코드 수
totalCount : 전체 조회 건수
eventName : 페이징 하단의 숫자 등의 버튼이 클릭되었을 때 호출될 함수 이름
*/
var gfv_pageIndex = null;
var gfv_eventName = null;
function gfn_renderPaging(params){
    var divId = params.divId; //페이징이 그려질 div id
    gfv_pageIndex = params.pageIndex; //현재 위치가 저장될 input 태그
    var totalCount = params.totalCount; //전체 조회 건수
    var currentIndex = $("#"+params.pageIndex).val(); //현재 위치
    if($("#"+params.pageIndex).length == 0 || currentIndex.trim() == "" ){
        currentIndex = 1;
    }
     
    var recordCount = params.recordCount; //페이지당 레코드 수
    if( recordCount == ""){
        recordCount = 20;
    }
    var totalIndexCount = Math.ceil(totalCount / recordCount); // 전체 인덱스 수
    gfv_eventName = params.eventName;
     
    $("#"+divId).empty();
    var preStr="", postStr="", str="", first, last, prev, next;
     
    first = (parseInt((currentIndex-1) / 10) * 10) + 1;
    if( totalIndexCount == 0 ) last = 0;
    else {
		last = first+9;
		if(last > totalIndexCount) last = totalIndexCount;
	}
    prev = (parseInt((currentIndex-1)/10)*10) - 9 > 0 ? (parseInt((currentIndex-1)/10)*10) - 9 : 1;
    next = (parseInt((currentIndex-1)/10)+1) * 10 + 1 < totalIndexCount ? (parseInt((currentIndex-1)/10)+1) * 10 + 1 : totalIndexCount;
    
    if(totalIndexCount > 10){ //전체 인덱스가 10이 넘을 경우, 맨앞, 앞 태그 작성
    	preStr += "<a href='javascript:void(0);' class='first'  onclick='fnPageMove(1)'>처음</a>";
    	preStr += "<a href='javascript:void(0);' class='prev' onclick='fnPageMove("+prev+")'>이전</a>";
    }
    else if(totalIndexCount <=10 && totalIndexCount > 1){ //전체 인덱스가 10보다 작을경우, 맨앞 태그 작성
    	/*preStr += "<a href='javascript:void(0);' class='first'  onclick='fnPageMove(1)'>처음</a>";*/
    }
     
    if(totalIndexCount > 10){ //전체 인덱스가 10이 넘을 경우, 맨뒤, 뒤 태그 작성
    	postStr += "<a href='javascript:void(0);' class='next' onclick='fnPageMove("+next+")'>다음</a>";
    	postStr += "<a href='javascript:void(0);' class='last' onclick='fnPageMove("+totalIndexCount+")'>마지막</a>";
    }
    else if(totalIndexCount <=10 && totalIndexCount > 1){ //전체 인덱스가 10보다 작을경우, 맨뒤 태그 작성
    	/*postStr += "<a href='javascript:void(0);' class='last' onclick='fnPageMove("+next+")'>마지막</a>";*/
    }
     
    //for(var i=first; i<(first+last); i++){
    for(var i=first; i<=last; i++){
        if(i != currentIndex){
            str += "<a href='javascript:void(0);' onclick='fnPageMove("+i+")'>"+i+"</a>";
            
        }else{
        	str += "<a href='javascript:void(0);' class='active' style='cursor:default;'>"+i+"</a>";
        }
    }
    $("#"+divId).append(preStr + str + postStr);
    $("#"+divId+"_TOP").html(preStr + str + postStr);
}
 
function fnPageMove(value){
    $("#"+gfv_pageIndex).val(value);
    if(typeof(gfv_eventName) == "function"){
        gfv_eventName(value);
    }else {
        eval(gfv_eventName + "(value);");
    }
}

/**
 * 페이징 처리
 * @param method 검색함수
 * @param totalCount 총 카운트
 * @returns
 */
function fnPagingPrc(method, totalCount){
	var params = {
            divId : "PAGE_NAVI",
            pageIndex : "pageIndex",
            totalCount : totalCount,
            recordCount : 10,
            eventName : method
        };
	gfn_renderPaging(params);
}
/*********************************************** 페이징 처리 End ***********************************************/


/**
 * POST 방식 페이지 이동
 * @param url
 * @param keyList
 * @param valueList
 * @returns
 */
function fnMovePage(url, keyList, valueList){
	var form = $('<form></form>');
	form.prop('action', url);
	form.prop('method', 'POST');
	form.appendTo('body');

	for( var i in keyList ){
		if( fnNotNullAndEmpty(keyList[i]) ){
			form.append('<input type="hidden" name="'+keyList[i]+'" value="' + valueList[i] + '">');
		}
	}
	form.submit();
}



/**
 * GET 방식 페이지 이동
 * @param url
 * @param keyList
 * @param valueList
 * @returns
 */
function fnMovePageWithGet(url, keyList, valueList){
	for( var i in keyList ){
		if(i==0){
			if( fnNotNullAndEmpty(keyList[i]) )	url += "?"+keyList[i]+"="+encodeURIComponent(valueList[i]);
		}else{
			if( fnNotNullAndEmpty(keyList[i]) )	url += "&"+keyList[i]+"="+encodeURIComponent(valueList[i]);
		}
	}
	location.href = url;
}


/**
 * Popup 페이지 이동
 * @param url
 * @param option
 * @param keyList
 * @param valueList
 * @returns
 */
function fnMovePopupPage(url, option, keyList, valueList){
	if( !fnNotNullAndEmpty(option) )
		option = "width=1024,height=768";
	var form = $('<form></form>');
    form.prop('action', url);
    form.prop('method', 'POST');
    form.prop('target', 'movePoupuPage');
    form.appendTo('body');
    
    for( var i in keyList ){
    	if( fnNotNullAndEmpty(keyList[i]) )
    		form.append('<input type="hidden" name="'+keyList[i]+'" value="' + valueList[i] + '">');
    }
    
	window.open(url, "movePoupuPage", option);
	form.submit();
}

/**
 * Get방식 페이지 이동
 * @param url
 * @param seq
 * @param option
 */
function fnGetMovePage(url, param1, param2){
	
	if( fnNotNullAndEmpty(param1) )	url += "?param1="+encodeURIComponent(param1);
	if( fnNotNullAndEmpty(param2) )	url += "&param2="+encodeURIComponent(param2);
	
	location.href = url;
}

/**
 * Null 또는 공백 또는 undefined일 아닐 경우 true 반환
 * @param val
 * @returns {Boolean}
 */
function fnNotNullAndEmpty(val) {
	if (typeof val == 'undefined')	return false;
	else if (val == null)	return false;
	else if (val == "null")	return false;
	else if ($.trim(val) == "")	return false;
	else if (val.length < 1)	return false;
	else return true;
}

/**
 * Null, 공백, undefined일 경우 separator로 반환
 * @param val
 * @param separator
 * @returns
 */
function fnReplaceNull(val, separator){
	var sp = separator != null ? separator : "";
	if (typeof val == 'undefined')	return sp;
	else if (val == null)	return sp;
	else if (val == "null")	return sp;
	else if (val.length < 1)	return sp;
	else return val;
}

/**
 * 파일 다운로드
 * @param url
 * @param fileName
 * @param filePath
 * @param checkDelete
 */
function fnFileDownload(url, fileName, filePath, checkDelete){
	var form = $('<form></form>');
    form.prop('action', url);
    form.prop('method', 'POST');
    form.appendTo('body');
	form.append('<input type="hidden" name="fileName" value="' + fileName + '">');
	form.append('<input type="hidden" name="filePath" value="' + filePath + '">');
	form.append('<input type="hidden" name="checkDelete" value="' + checkDelete + '">');
	form.submit();
}


/**
 *  yyyy-MM-dd 포맷으로 반환(separator)
 */
function getFormatDate(date, sep){
	var separator = "-";
	if( fnNotNullAndEmpty(sep) )	separator = sep;
	
	var year = date.getFullYear();             //yyyy
	var month = (1 + date.getMonth());         //M
	month = month >= 10 ? month : '0' + month; // month 두자리로 저장
	var day = date.getDate();                  //d
	day = day >= 10 ? day : '0' + day;         //day 두자리로 저장
	return  year + separator + month + separator + day;
}

/**
 * 날짜 변경
 * @param option
 * @param fm
 * $.datepicker.formatDate 안될때...
 */
function fnGetDate(option, fm){
	var nowDate = new Date();
	var format = "yy-mm-dd";
	
	if( fm != null )	format = fm;

	var endDate = getFormatDate(nowDate);
	
	var startDate = "";
	
	if( option === "lastWeek" ){
		nowDate.setDate(nowDate.getDate() - 7);
		startDate = getFormatDate(nowDate);
		
	}else if( option === "lastTwoWeek" ){
		nowDate.setDate(nowDate.getDate() - 14);
		startDate = getFormatDate(nowDate);
		
	}else if( option === "lastThreeWeek" ){
		nowDate.setDate(nowDate.getDate() - 21);
		startDate = getFormatDate(nowDate);
		
	}else if( option === "lastMonth" ){
		nowDate.setMonth(nowDate.getMonth() -1 );
		startDate = getFormatDate(nowDate);
		
	}else if( option === "lastThreeMonth" ){
		nowDate.setMonth(nowDate.getMonth() -3 );
		startDate = getFormatDate(nowDate);
		
	}else if( option === "lastYear" ){
		nowDate.setFullYear(nowDate.getFullYear() - 1);
		startDate = getFormatDate(nowDate);
		
	}else if( option === "today" ){
		startDate = getFormatDate(nowDate);
	}
	
	var date = {
		"searchStartDate"	: 	startDate,
		"searchEndDate"		:	endDate
	}
	return date;
}



/**
 * 현재 날짜와 비교 후 true/false 반환
 * @param date
 * @returns {Boolean}
 */
function fnCompareNowDate(date){
	var result = false;
	if( date != null ){
		var nowDate = new Date();
		var expireDate = new Date(date);
		if( expireDate > nowDate ) result = true;
	}
	return result;
}


/**
 * 에러 메시지창
 */
function fnErrorMessage(error){
	alert("서비스가 원활하지 않습니다. 잠시뒤에 이용해주시길 바랍니다.");
	console.log(error);
}

/*숫자만 입력가능*/
function fnOnlyNumber(event){
    event = event || window.event;
 
    var keyID = (event.which) ? event.which : event.keyCode;
    
    if ( (keyID >= 48 && keyID <= 57) || (keyID >= 96 && keyID <= 105) || keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39 ) {
        return;
    } else {
        return false;
    }
}

/*숫자, 콤마 입력*/
function fnOnlyNumberDot(event){
    event = event || window.event;
 
    var keyID = (event.which) ? event.which : event.keyCode;
    // Comma keyID == 188 , Dot keyID == 190
    if ( keyID == 190 || (keyID >= 48 && keyID <= 57) || (keyID >= 96 && keyID <= 105) || keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39) {
        return;
    } else {
        return false;
    }
}

/*숫자 . , / *  입력*/
function fnOnlyNumberCommaDot(event){
    event = event || window.event;
    var keyID = (event.which) ? event.which : event.keyCode;
    // Comma keyID == 188 , Dot keyID == 190
    if ( keyID == 188 || keyID == 190 || keyID == 191 || keyID == 56 || (keyID >= 48 && keyID <= 57) || (keyID >= 96 && keyID <= 105) || keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39) {
        return;
    } else {
        return false;
    }
}

/* 숫자, 콤마 제외한 나머지 제거 */
function fnRemoveChar(event) {
    event = event || window.event;
     
    var keyID = (event.which) ? event.which : event.keyCode;
     
    if ( keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39 ) {
        return;
    } else {
        event.target.value = event.target.value.replace(/[^0-9|^.]/g, "");
    }
}

/**
 * 긴 단어/문장 변경
 * @param text
 * @param separator
 * @param limit
 * @returns
 */
function fnLongWordTranslation(text, separator, limit){
	if( !fnNotNullAndEmpty(separator) ) separator = "...";
	if( !fnNotNullAndEmpty(limit) ) limit = 20;
	
	if( fnNotNullAndEmpty(text) && (text.length > limit) ){
		text = text.substring(0,limit) + separator;
	}
	return text
}

/**
 * 3자리 콤마 찍기
 * @param text
 * @returns
 */
var numberWithCommas = function(x){
	if( fnNotNullAndEmpty(x) ){
		return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");	
	}else{
		return "";
	}	 
}

/**
 * Null을 -1로 변경
 * @param val
 * @returns
 */
function fnChangeNullToNum(val){
	if (typeof val == 'undefined')
		return -1;
	else if (val == null)
		return -1;
	else if (val.length < 1)
		return -1;
	
	return val;
}

/**
 * Object sort
 * @param obj
 * @param option
 * @returns
 */
function fnSortObj(obj, option){
	if( fnNotNullAndEmpty(obj) ){
		// keyword sort asc
		obj.sort(function(a, b){
			if( a.keyword > b.keyword )	return 1;
			if( a.keyword < b.keyword )	return -1;
			return 0;
		});
		
		// keyword_id sort desc
		obj.sort(function(a, b){
			if( fnChangeNullToNum(a.keyword_id) < fnChangeNullToNum(b.keyword_id) )	return 1;
			if( fnChangeNullToNum(a.keyword_id) > fnChangeNullToNum(b.keyword_id) )	return -1;
			return 0;
		});
		
		// weight sort desc
		obj.sort(function(a, b){
			if( fnChangeNullToNum(a.weight) < fnChangeNullToNum(b.weight) )	return 1;
			if( fnChangeNullToNum(a.weight) > fnChangeNullToNum(b.weight) )	return -1;
			return 0;
		});
		
		if( option == "string" ){
			return fnArrValueToString(obj, "keyword", " ,", 5);
		}else{
			return obj;
		}
	}
}


/**
 * 관련기관 및 인문 sort
 * @param personOrgan
 * @param option
 * @returns
 */
function fnSortPersonOrgan(personOrgan, option){
	if( fnNotNullAndEmpty(personOrgan) ){
		// name sort asc
		personOrgan.sort(function(a, b){
			if( a.name > b.name )	return 1;
			if( a.name < b.name )	return -1;
			return 0;
		});
		
		// type sort desc
		personOrgan.sort(function(a, b){
			if( a.type < b.type )	return 1;
			if( a.type > b.type )	return -1;
			return 0;
		});
		
		if( option == "string" ){
			return fnArrValueToString(personOrgan, "name", " ,", 5);
		}else{
			return personOrgan;
		}
	}else{
		return "-";
	}
}

/**
 * 배열의 value를 한줄로 생성
 * @param dataArr
 * @param value
 * @param separator
 * @param count
 * @returns {String}
 */
function fnArrValueToString(dataArr, value, separator, count){
	var result = "-";
	if( fnNotNullAndEmpty(dataArr) ){
		for(var index in dataArr ){
			if( fnNotNullAndEmpty(value) && value === "keyword" && fnNotNullAndEmpty(dataArr[index].keyword)){
				if( index == 0 ) result = dataArr[index].keyword;
				else result += separator+"&nbsp;"+dataArr[index].keyword;
				
			}else if( fnNotNullAndEmpty(value) && value === "name" && fnNotNullAndEmpty(dataArr[index].name)){
				if( index == 0 ) result = dataArr[index].name;
				else result += separator+"&nbsp;"+dataArr[index].name;
				
			}else if( fnNotNullAndEmpty(dataArr[index]) && value == null ){
				if( index == 0 ) result = dataArr[index];
				else result += separator+"&nbsp;"+dataArr[index];	
			}
			
			if( fnNotNullAndEmpty(count) && index == count ) break;
		}
	}
	
	return result;
}

//한글체크
var fnCheckKorean = function(value){
	var check = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/;
	if( check.test(value) ){
		return true;
	}else{
		return false;
	}
}


/*********************************************** Common function end ***********************************************/


/*********************************************** project function start ***********************************************/

/**
 * Notification
 * type = info, success, warning, danger
 * @param type
 * @param text
 * @returns
 */
var fnComNotify = function(type, text){
	new PNotify({
        title: type,
        text: text,
        type: type,
        styling: 'bootstrap3'
    });
}


/**
 * fnComErrorMessage
 * @param text
 * @param message
 * @returns
 */
var fnComErrorMessage = function(text, message){
	fnComNotify("error",text);
	console.log(text+" : ");
	console.log(message);
}

/*목록 체크된 값 가져오기*/
var fnTableCheckList = function(id){
	var checkMap = {};
	var checkIdList = [];
	var checkRowList = [];
	$("#"+id).find("input[name='table_records']").each(function(){
		if( $(this).is(":checked") ){
			checkRowList.push($(this).parent().parent().parent());
			checkIdList.push($(this).attr("id"));
		}
	});
	
	checkMap["checkIdList"] = checkIdList;
	checkMap["checkRowList"] = checkRowList;
	
	return checkMap;
}

/* 테이블 삭제 */
var fnComDeleteTable = function(id,checkRowList){
	for( var i in checkRowList ){
		$("#"+id).dataTable().fnDeleteRow(checkRowList[i]);
	}
}

/*타입에 따른 validation*/
var fnCheckTypeValue = function(type, value){
	var result = false;
	if( type == "string" || type == "bool" || type == "none" ){
		return true;
	}else{
		switch (type) {
			case "int" : 		  // '예) 5'
				if( value.replace(/^[0-9]+$/, "") == "" ) result = true; break;
				
			case "float" : 		  // 예) 5.5
				if( value.replace(/^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "" ) result = true; break;
				
			case "numerical" :    // 예) 5.5
				if( value.replace(/^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "" ) result = true; break;
				
			case "string, numerical" : // 예) String or 8 
				if( value.replace(/^[a-zA-Z]+|^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "" ) result = true; break;
				
			case "numerical, string, np.nan" : // 예) String or 8.8
				if( value.replace(/^[a-zA-Z]+|^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "" ) result = true; break;
				
			case "string, int, array" :  // 예) String or 1 or [String,String] or [3,5]
				if( value.replace(/^[a-zA-Z]+|^\d+|^\[+[a-zA-Z,]+\]+|^\[+[0-9,]+\]+$/, "") == "" ) result = true; break;
				
			case "string, array" :  // 예) String or [String,String]
				if( value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "string, list of ints, array" :  // 예) String or [3,5] or [String,String] or [3,5]
				if( value.replace(/^[a-zA-Z]+|^\[+[a-zA-Z,]+\]+|^\[+[0-9,]+\]+$/, "") == "" ) result = true; break;
				
			case "string, list, array" :	// 예) String or [String,String]
				if( value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "string, list of lists, array" :	// 예) String or [String,String] or [[String,String],[String,String]]
				if( value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "array-like" :		//예) [String,String] or [[String,String],[String,String]]
				if( value.replace(/^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "int, array-like" :	// 예) 2 or [3,5]
				if( value.replace(/^[0-9]+|^\[+[0-9,]+\]+$/, "") == "" ) result = true; break;
				
			case "string, sparse matrix" :	// 예) [[String,String],[String,String]]
				if( value.replace(/^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "array" :			// 예) [3]
				if( value.replace(/^\[+[0-9,]+\]+$/, "") == "" ) result = true; break;
				
			case "tuple" :			// 예) (2,3)
				if( value.replace(/^\(+[0-9,-]+\)+$/, "") == "" ) result = true; break;
				
			case "int, string, list" : // 예) 2 or String or [3,5] or [String,String]
				if( value.replace(/^[0-9]+|^\[+[0-9,]+\]|^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "int, list" :		// 예) 2 or [3,5]
				if( value.replace(/^[0-9]+|^\[+[0-9,]+\]+$/, "") == "" ) result = true; break;
				
			case "string, list" :	// 예) String or [String,String]
				if( value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
			
			case "string, list of list, array" :	// 예) String or [String,String]
				if( value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "" ) result = true; break;
				
			case "int, string" : 	// 예) 2 or String
				if( value.replace(/^[a-zA-Z]+|^\d+$/, "") == "" ) result = true; break;
			default : ""; break;
		}
		
		return result;
	}
}

/*날짜 포멧 체크*/
var fnDateFormatCheck = function(value){
	var expText = /\{+([YYYY|yyyy]{4})+([.:\-_@])*([MM]{2})+(([.:\-_@])*([dd]{2})+)*(([.:\-_@])*([HH]{2})+(([.:\-_@])*([mm]{2})+)*)*\}+/;
	if( expText.test(value) ){
		return false;
	}
	return true
}

var fnArraySplitBR  = function(value){
	if( $.isArray(value) ){
		var result = "";
		for( var i in value ){
			if( i == 0 ){
				result += value[i];
			}else if( i%5 == 0 ){
				result += ", <br>"+value[i];
			}else{
				result += ", "+value[i];
			}
		}
		return result;
	}else{
		return value;
	}
}


/*체크박스 체크해지*/
var fnUnCheckbox = function(id){
	$("#check-all").prop("checked", false);
	if( fnNotNullAndEmpty(id) )	$("#"+id).prop("checked", false);
	
	$("input[name='table_records']").each(function(){
		$(this).prop("checked", false);
	});
}


/*프로젝트 목록 가져오기*/
var fnGetProject = function(){
	var html = "";
	var userProjectList = fnGetProjectListByAjax();
	for( var i in userProjectList ){
		var project = userProjectList[i];
		if( html == "" ){
			fnGetModelsOfProjectPk(project.PROJECT_SEQUENCE_PK);
			html += "<li class='projectList active pointerCorsor' data-projectSequencePk="+project.PROJECT_SEQUENCE_PK+">"+project.NAME+"</li>";
		}else{
			html += "<li class='projectList pointerCorsor' data-projectSequencePk="+project.PROJECT_SEQUENCE_PK+">"+project.NAME+"</li>";
		}
	}
	
	$("#selectedProject").html(html);
}

/* 프로젝트 목록 가져오기(배치에 등록된 프로젝트 선택)*/
var fnGetSavedProject = function(batchProjectSequencePk, batchModelSequencePk){
	var html = "";
	var userProjectList = fnGetProjectListByAjax();
	for( var i in userProjectList ){
		var project = userProjectList[i];
		if (project.PROJECT_SEQUENCE_PK == batchProjectSequencePk){
			fnGetSavedModelsOfProjectPk(batchProjectSequencePk, batchModelSequencePk);
			html += "<li class='projectList active pointerCorsor' data-projectSequencePk="+project.PROJECT_SEQUENCE_PK+">"+project.NAME+"</li>";
		} else {
			html += "<li class='projectList pointerCorsor' data-projectSequencePk="+project.PROJECT_SEQUENCE_PK+">"+project.NAME+"</li>";
		}
	}
	
	$("#selectedProject").html(html);
}

/* 프로젝트별 모델 목록 가져오기*/
var fnGetModelsOfProjectPk = function(projectSequencePk, option){
	var html = "";
	var modelList = fnGetModelsByAjax(projectSequencePk, "");
	for( var i in modelList ){
		var model = modelList[i];
		
		// 성공인 모델만... 
		if( option == "useOfBatch" && model.PROGRESS_STATE == "success" ){
			if( html == "" ){
				html += "<li class='modelList active pointerCorsor' data-modelSequenceFk1="+model.MODEL_SEQUENCE_PK
					 +" data-instanceSequenceFk2="+model.INSTANCE_SEQUENCE_FK3
					 +" data-projectSequenceFk3="+model.PROJECT_SEQUENCE_FK4+">"+model.NAME+"</li>";
			}else{	
				html += "<li class='modelList pointerCorsor' data-modelSequenceFk1="+model.MODEL_SEQUENCE_PK
				     +" data-instanceSequenceFk2="+model.INSTANCE_SEQUENCE_FK3
					 +" data-projectSequenceFk3="+model.PROJECT_SEQUENCE_FK4+">"+model.NAME+"</li>";
			}
		}else{
			if( html == "" ){
				html += "<li class='modelList active pointerCorsor' data-modelSequenceFk1="+model.MODEL_SEQUENCE_PK
					 +" data-instanceSequenceFk2="+model.INSTANCE_SEQUENCE_FK3
					 +" data-projectSequenceFk3="+model.PROJECT_SEQUENCE_FK4+">"+model.NAME+"</li>";
			}else{	
				html += "<li class='modelList pointerCorsor' data-modelSequenceFk1="+model.MODEL_SEQUENCE_PK
				     +" data-instanceSequenceFk2="+model.INSTANCE_SEQUENCE_FK3
					 +" data-projectSequenceFk3="+model.PROJECT_SEQUENCE_FK4+">"+model.NAME+"</li>";
			}
		}
	}
	if( html == "" ) html += "<li class='modelList'>모델이 없습니다.</li>";
	
	$("#selectedModel").html(html);
}


/* 프로젝트별 모델 목록 가져오기(배치에 등록된 프로젝트 모델 선택)*/
var fnGetSavedModelsOfProjectPk = function(batchProjectSequencePk, batchModelSequencePk){
	var html = "";
	var modelList = fnGetModelsByAjax(batchProjectSequencePk, "");
	for( var i in modelList ){
		var model = modelList[i];
		if( model.MODEL_SEQUENCE_PK == batchModelSequencePk){
			html += "<li class='modelList active pointerCorsor' data-modelSequenceFk1="+model.MODEL_SEQUENCE_PK
					+" data-instanceSequenceFk2="+model.INSTANCE_SEQUENCE_FK3
					+" data-projectSequenceFk3="+model.PROJECT_SEQUENCE_FK4+">"+model.NAME+"</li>";
		}else{	
			html += "<li class='modelList pointerCorsor' data-modelSequenceFk1="+model.MODEL_SEQUENCE_PK
					+" data-instanceSequenceFk2="+model.INSTANCE_SEQUENCE_FK3
					+" data-projectSequenceFk3="+model.PROJECT_SEQUENCE_FK4+">"+model.NAME+"</li>";
		}
	}
	if( html == "" ) html += "<li class='modelList'>모델이 없습니다.</li>";
	
	$("#selectedModel").html(html);
}



/*도메인명 가져오기*/
var fnGetRequestTemplateAvailable = function(){
	var html = "";
	var availableList = fnGetRequestTemplateAvailableByAjax();
	for( var i in availableList ){
		html += "<option value="+availableList[i].id+">"+(availableList[i].name==""?availableList[i].id:availableList[i].name)+"</option>";
	}
	$("#resultUpdateDomain").html(html);
}


//-----------------------------------------------------------
//tabmenu
//-----------------------------------------------------------
function fnSetTabMenu(tabName, num){

  var num = num || 0;
  var menu = $(tabName).children();
  var con = $(tabName+'_con').children();
  var select = $(menu).eq(num);
  var i = num;

	menu.click(function(){
		menu.removeClass('on');
		con.removeClass('tabViewOn');

      if(select!==null){
          select.removeClass("on");
          //con.eq(i).hide();
      }

      select = $(this);	
      i = $(this).index();

      select.addClass('on');
      con.eq(i).addClass('tabViewOn');
  });
}

var language = {
	"emptyTable": "데이터가 없습니다.",
	"lengthMenu": "_MENU_",
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