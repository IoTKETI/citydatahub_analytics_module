var userRole, projectSequencePk, selectedInstancePk, selectedOriginalDataPk, selectedPreprocessedDataPk, selectedModelPk;
var preprocessTestFlug = false;
var preprocessRequestTest;
$("#loading").show();
$(function(){
	fnInit();
	
	// 인스턴스 로컬파일 클릭시
	$(document).on("click", ".localFiles", function(){
		/*파일브라우저 샘플 미리보기*/
		$(".localFiles").removeClass("activeSel");
		$(this).addClass("activeSel");
		fnGetLocalFileSample($(this).text());
	});
	
	// 원본리스트 클릭시
	$(document).on("click", ".originalData", function(){
		$("#loading").show();
		/*파일브라우저 샘플 미리보기*/
		$(".originalData").removeClass("activeSel");
		$(this).addClass("activeSel");
		selectedOriginalDataPk = $(this).attr("id");
		fnGetOriginalData();
	});
	
	// 전처리 처리방식 변경시
	$(document).on("change", "#preprocessFunction", function(){
		fnGetPreprocessFunctionParameters($(this).val());
	});
	
	// 파라미터 변경시
	$(document).on("change", "#parameters", function(){
		fnSetParamType($(this).val(),$(this).children("option:selected").attr("data-enumerate"),$(this).children("option:selected").attr("data-default"));
	});
	
	// 전처리 모달의 등록된 전처리 리스트 삭제버튼
	$(document).on("click", ".deletePreprocessBtn", function(){
		$(this).parent().parent().remove();
		$("#preprocessTest").text("");
		preprocessTestFlug = false;
	});
	
	// inputValue
	$("#inputValue").bind("keypress", function(e){
		if( e.which == 13 )	fnAddParams();
	});
	
	/*원본데이터 차트 변경시*/
	$(document).on("change", "#originalDataChartName", function(){
		fnOriginalChart(JSON.parse($(this).val()));
	});
	
});

var createTable = function(){
	// 공통 호출 에러 방지 Dummy
}

var fnInit = function(){
	$(".breadcrumb__list--current").text("프로젝트 상세");
	userRole = $("#userRole").val();
	projectSequencePk = $("#projectSequencePk").val();
	
	// 프로젝트 정보를 가져온다.
	fnGetProjectInfo(projectSequencePk);
	
	// 원본 리스트 가져오기
	fnGetOriginalDataList(projectSequencePk);
	
}


/*프로젝트 정보 가져오기*/
var fnGetProjectInfo = function(projectSequencePk){
	var project = fnGetProjectByAjax(projectSequencePk);
	$(".name").text(project.NAME);
	$("#description").text(project.DESCRIPTION);
	$("#createDataTime").text("생성일: "+project.createDataTime);
	selectedInstancePk = project.SELECTED_INSTANCE;
}


/*원본 리스트 가져오기*/
var fnGetOriginalDataList = function(projectSequencePk){
	var originalDataList = fnGetOriginalDataListByAjax(projectSequencePk);
	var html = "";
	var originalDataName = "";
	for( var i in originalDataList ){
		var oData = originalDataList[i];
		originalDataName = oData.NAME;
		if( originalDataName.length > 40 ) originalDataName = originalDataName.substring(0,40)+"...";
		if( i == 0 ){
			html += "<li class='originalData activeSel' id="+oData.ORIGINAL_DATA_SEQUENCE_PK+" title='"+oData.NAME+"'><a href='javascript:'>"+originalDataName+"</a></li>";
			
			selectedOriginalDataPk = oData.ORIGINAL_DATA_SEQUENCE_PK;
			fnGetOriginalData(oData);/* 원본데이터 가져오기*/
			
		}else{
			html += "<li class='originalData' id="+oData.ORIGINAL_DATA_SEQUENCE_PK+" title='"+oData.NAME+"'><a href='javascript:'>"+originalDataName+"</a></li>";
			
		}
	}
	if( html == "" ){
		html += "<li>원본데이터가 없습니다.</li>";
		$("#originalDataDiv").hide();
		
		// 전처리 리스트 가져오기
		fnGetPreprocessedDataList();
	}else{
		$("#deleteOriginalDataBtn").show();
	}
	$("#originalDataList").html(html);
	$("#loading").hide();
}

/* 원본데이터 가져오기 */
var fnGetOriginalData = function(oData){
	if( oData == null ){
		oData = fnGetOriginalDataByAjax(projectSequencePk, selectedOriginalDataPk);
	}
	$(".originalDataName").text(oData.NAME);
	$("#originalDataFileName").text(oData.FILENAME);
	$("#originalDataFilePath").text(oData.FILEPATH);
	$("#originalDataAmount").text(numberWithCommas(oData.AMOUNT));		
	$("#originalDataCreateDateTime").text(oData.CREATE_DATETIME.substring(0,19).replace("T"," "));

	var statistics = oData.STATISTICS;
	var html = "";
	for( var i in statistics ){
		if( "datetime" != statistics[i].name ){
			if( html == "" )	fnOriginalChart(statistics[i]);
			html += "<option value='"+JSON.stringify(statistics[i])+"'>"+statistics[i].name+"</option>";
		}
	}
	$("#originalDataChartName").html(html);
	$("#originalDataDiv").fadeIn();
	
	// 전처리 리스트 가져오기
	fnGetPreprocessedDataList();
}

/* 원본데이터 삭제*/
var fnDeleteOriginalData = function(){
	if( confirm("원본 데이터를 삭제하면 생성된 전처리, 모델이 같이 삭제됩니다. \n삭제하시겠습니까?") ){
		var result = fnDeleteOriginalDataByAjax(projectSequencePk, selectedOriginalDataPk);
		if( result == "success" ){
			fnComNotify("success", "원본데이터를 삭제 하였습니다.");
			// 원본리스크 가져오기
			fnGetOriginalDataList(projectSequencePk);
		}
	}
}

/*원본 데이터 생성버튼 클릭시 파일브라우저 가져오기*/
var fnGetSandboxFileBrowser = function(){
	var response = fnGetSandboxFileBrowserByAjax(selectedInstancePk);
	
	if( response.result == "success" ){
		var localFiles = response.localFiles;
		var html = "";
		for( var i in localFiles ){
			if( i == 0 ){
				fnGetLocalFileSample(localFiles[i]);
				html += "<li class='localFiles activeSel' title='"+localFiles[i]+"'><a>"+localFiles[i]+"</a></li>";
			}else{
				html += "<li class='localFiles' title='"+localFiles[i]+"'><a>"+localFiles[i]+"</a></li>";
			}
		}
		$("#localFiles").html(html);
		fnOpenModal("originModal");
	}else {
		fnComNotify("warning","원본 데이터 생성을 실패하였습니다.");
	}
}

/*파일브라우저 샘플 미리보기*/
var fnGetLocalFileSample = function(localFile){
	var localFile = fnGetLocalFileSampleByAjax(selectedInstancePk, localFile);
	$("#localFileSample").html("<pre>"+JSON.stringify(localFile,null,2)+"</pre>");
}

/*원본데이터 생성*/
var fnCreateOriginalData = function(){
	var localFile = "";
	var data = {};
	$(".localFiles").each(function(){
		if( $(this).hasClass("activeSel") ){
			localFile = $(this).text();
		}
	});
	
	if( fnNotNullAndEmpty(localFile) ){
		if( confirm("생성하시겠습니까?") ){
			data["filename"] = localFile;
			data["projectSequenceFk1"] = projectSequencePk;
			data["instanceSequenceFk2"] = selectedInstancePk;
	
			var result = fnCreateOriginalDataByAjax(projectSequencePk, data);
			if( result.result == "success" ){
				fnComNotify("success", "학습용 원본 데이터를 생성하였습니다.");
				fnCloseModal("originModal");
				// 원본리스크 가져오기
				fnGetOriginalDataList(projectSequencePk);
				
			}else if( result.detail == "duplicateName"){
				fnComNotify("warning",localFile+"은 이미 생성되었습니다.");
			
			}else{
				fnComErrorMessage("원본데이터 생성 에러", response.detail);
			}
		}
		
	}else{
		fnComNotify("warning", "파일을 선택해주세요.");
	}
}

/*전처리 모달 생성*/
var fnPreprocessingModal = function(){
	if( fnNotNullAndEmpty(selectedOriginalDataPk) ){
		preprocessTestFlug = false;
		// 초기화
		$("#preprocessListTbody").children().remove();
		$("#preprocessTest").text("");
		
		// 필드명 가져오기
		fnGetColumns();
		
		// 처리방식 가져오기
		fnGetPreprocessFunction();

		// preprocessingModal 모달 생성
		fnOpenModal("preprocessingModal");
		
	}else{
		fnComNotify("warning","원본데이터를 생성해주세요.");
	}
}

/*필드명 가져오기*/
var fnGetColumns = function(){
	var originalData = fnGetOriginalDataByAjax(projectSequencePk, selectedOriginalDataPk);
	var columns = originalData.COLUMNS;
	columns.sort();
	var sampleData = originalData.SAMPLE_DATA;
	var html = "";
	for( var i in columns ){
		html += "<option>"+columns[i]+"</option>"
	}
	$("#columns").html(html);
	$("#sampleData").html(JSON.stringify(originalData.SAMPLE_DATA,null,2));
}

/*처리방식 가져오기*/
var fnGetPreprocessFunction = function(){
	var preprocessFunctionList = fnGetPreprocessFunctionByAjax();
	var html = "";
	for( var i in preprocessFunctionList ){
		if( i == 0 )	fnGetPreprocessFunctionParameters(preprocessFunctionList[i].PREPROCESS_FUNCTION_SEQUENCE_PK);
		
		html += "<option value="+preprocessFunctionList[i].PREPROCESS_FUNCTION_SEQUENCE_PK+" " +
				"title='"+preprocessFunctionList[i].LIBRARY_FUNCTION_DESCRIPTION+"'>"
				+preprocessFunctionList[i].PREPROCESS_FUNCTION_NAME+"</option>"
	}
	$("#preprocessFunction").html(html);
}

/*파라미터 가져오기*/
var fnGetPreprocessFunctionParameters = function(preprocessFunctionSequencePk){
	var parameters = fnGetPreprocessFunctionParametersByAjax(preprocessFunctionSequencePk);
	var html = "";
	
	for( var i in parameters ){
		if( fnNotNullAndEmpty(parameters[i].name) ){
			if( i == 0 )	fnSetParamType(parameters[i].type, parameters[i].enumerate, parameters[i].default);
			
			html += "<option value='"+parameters[i].type+"' title='"+parameters[i].note+"' data-enumerate='"+parameters[i].enumerate+"' data-default='"+parameters[i].default+"'>"
					+parameters[i].name+"</option>";
		}
	}
	if( html == "" ){
		html = "<option value='none'>없음</option>";
		$("#valueLabel").hide();
		$("#selectType").hide();
		$("#inputType").hide();
	}
	$("#parameters").html(html);
}

/*파라미터 타입별 세팅*/
var fnSetParamType = function(type, enumerate, defaultValue){
	var html = "";
	if( type == "string" ){
		var e = enumerate.split(",");
		for( var i in e ){
			html += "<option>"+e[i]+"</option>";
		}
		$("#selectValue").html(html);
		$("#inputType").hide();
		$("#selectType").fadeIn();
		$("#valueLabel").fadeIn();
		
	}else if( type == "bool" ){
		html += "<option>true</option><option>false</option>";
		$("#selectValue").html(html);
		$("#inputType").hide();
		$("#selectType").fadeIn();
		$("#valueLabel").fadeIn();
		
	}else if( type == "none" ){
		$("#selectType").hide();
		$("#inputType").hide();
		$("#valueLabel").hide();
				
	}else{
		$("#inputValue").val(defaultValue.replace(/ /g, ""));
		$("#inputValue").prop("placeholder",fnSetParamTypePlaceholder(type)); // 타입별 예제 적용
		$("#selectType").hide();
		$("#inputType").fadeIn();
		$("#valueLabel").fadeIn();
	}
}

/*타입별 예제 적용*/
var fnSetParamTypePlaceholder = function(type){
	var placeholder = "";
	switch (type) {
		case "int" : placeholder = '예) 5'; break;
		case "float" : placeholder = '예) 5.5'; break;
		case "numerical" : placeholder = '예) 5.5'; break;
		case "string, numerical" : placeholder = '예) String or 8'; break;
		case "numerical, string, np.nan" : placeholder = '예) String or 8.8 '; break;
		case "string, int, array" : placeholder = '예) String or 1 or [String,String] or [3,5]'; break;
		case "string, array" : placeholder = '예) String or [String,String]'; break;
		case "string, list of ints, array" : placeholder = '예) String or [3,5] or [String,String] or [3,5]'; break;
		case "string, list, array" : placeholder = '예) String or [String,String]'; break;
		case "string, list of lists, array" : placeholder = '예) String or [String,String] or [[String,String],[String,String]]'; break;
		case "array-like" : placeholder = '예) [String,String] or [[String,String],[String,String]]'; break;
		case "int, array-like" : placeholder = '예) 2 or [3,5]'; break;
		case "string, sparse matrix" : placeholder = '예) [[String,String],[String,String]]'; break;
		case "array" : placeholder = '예) [3]'; break;
		case "tuple" : placeholder = '예) (2,3)'; break;
		case "int, string, list" : placeholder = '예) 2 or String or [3,5] or [String,String]'; break;
		case "int, list" : placeholder = '예) 2 or [3,5]'; break;
		case "string, list" : placeholder = '예) String or [String,String]'; break;
		case "int, string" : placeholder = '예) 2 or String'; break;
		default : ""; break;
	}
	return placeholder;
}

/*선택된 전처리 리스트에 담기*/
var fnAddParams = function(){
	var html = '<tr>';
	var type = $("#parameters option:selected").val();
	var value = "";
	if( type == "string" || type == "bool" ){
		value = $("#selectValue option:selected").val();
		
	}else{
		value = $("#inputValue").val();
	}
	
	if( fnCheckAddParam() ){
		if( type == "none" || value != "" ){
			// 타입에 맞는지 체크
			if( fnCheckTypeValue(type, value) ){
				html += '<td>'+$("#columns option:selected").val()+'</td>';
				html += '<td data-preprocessPk="'+$("#preprocessFunction option:selected").val()+'">'+$("#preprocessFunction option:selected").text()+'</td>';
				html += '<td data-type="'+type+'">'+$("#parameters option:selected").text()+'</td>';
				html += '<td>'+value+'</td>';
				html += '<td><button type="button" class="float_none button__file button_default deletePreprocessBtn"><i class="fa fa-trash-o"> 삭제</i></button></td>';
				$("#preprocessListTbody").append(html);
				$("#preprocessTest").text("");
				preprocessTestFlug = false;
				
			}else{
				fnComNotify("warning","Value의 형식이 다릅니다. 예를 참고해서 작성해주세요.");
				$("#inputValue").focus();
			}
			
		}else{
			fnComNotify("warning","Value에 값을 입력해주세요.");
			$("#inputValue").focus();
		}
	}else{
		fnComNotify("warning","등록되어 있습니다.");
	}
}

/*전처리 리스트에 담기 체크*/
var fnCheckAddParam = function(){
	var result = true;
	var fildName = $("#columns option:selected").val();
	var functionName = $("#preprocessFunction option:selected").text();
	var functionParam = $("#parameters option:selected").text();
	
	$("#preprocessListTbody").find("tr").each(function(){
		if( fildName == $(this).children().eq(0).text() 
				&& functionName == $(this).children().eq(1).text()
				&& functionParam == $(this).children().eq(2).text() ){
			
			result = false;
		}
	});
	
	return result;
}

/*전처리 테스트*/
var fnPreprocessTest = function(){
	var paramData = {};
	preprocessRequestTest = [];
	
	$("#preprocessListTbody").find("tr").each(function(){
		var data = {};
		var condition = {};
		var conditionKey = "";
		var conditionValue = "";
		
		data["field_name"] = $(this).children().eq(0).text();
		data["preprocess_functions_sequence_pk"] = $(this).children().eq(1).attr("data-preprocessPk");
		if( "없음" != $(this).children().eq(2).text() )		conditionKey = $(this).children().eq(2).text();
		if( "" != conditionKey ){
			conditionValue = $(this).children().eq(3).text();
			condition[conditionKey] = conditionValue;
			data["condition"] = condition;
		}
		
		/*같은 파라미터 병합*/
		for( var i in preprocessRequestTest ){
			var tempData = preprocessRequestTest[i];
			if( tempData.preprocess_functions_sequence_pk == data.preprocess_functions_sequence_pk 
					&& tempData.field_name == data.field_name ){
				
				var tempCondition = tempData.condition;
				if( "" != conditionKey ){
					tempCondition[conditionKey] = conditionValue;
					tempData["condition"] = tempCondition;
					preprocessRequestTest[i] = tempData;
					data = null;
				}
			}
		}
		
		if( data != null )	preprocessRequestTest.push(data);
	});
	
	paramData["request_test"] = preprocessRequestTest;
	
	if( preprocessRequestTest.length > 0 ){
		var response = fnPreprocessTestByAjax(projectSequencePk, selectedOriginalDataPk, paramData);
		if( response.result == "success" ){
			fnComNotify("success", "테스트를 완료하였습니다.");
			$("#preprocessTest").text(JSON.stringify(response.data,null,2));
			preprocessTestFlug = true;
			
		}else if( response.type == "4000" ){
			var detail = response.data.detail;
			fnComErrorMessage("전처리 테스트 에러 \n"+detail, response);
			
		}else{
			$("#preprocessTest").text("");
			preprocessTestFlug = false;
			fnComErrorMessage("전처리 테스트 에러", response);
		}
	}else{
		fnComNotify("warning","전처리를 추가해주세요.");
	}
}

/*전처리 생성*/
var fnCreatePreprocess = function(){
	if( preprocessTestFlug ){
		var paramData = {
			"original_data_sequence_pk" : selectedOriginalDataPk
			,"request_data" : preprocessRequestTest
		};
		
		if( confirm("전처리를 생성하겠습니까?") ){
			var response = fnCreatePreprocessByAjax(projectSequencePk, paramData);
			if( response.result == "success" ){
				// 전처리 리스트 조회
				fnGetPreprocessedDataList();
				// 모델 리스트 조회
				fnGetModelList();
				fnComNotify("success", "전처리 생성 요청을 완료하였습니다.");
				fnCloseModal("preprocessingModal");
			}else{
				fnComErrorMessage("전처리 생성 에러", response.detail);
			}
		}
		
	}else{
		fnComNotify("warning","전처리 테스트를 완료되어야 생성 가능합니다.");
	}
}

/*원본데이터 차트*/
var fnOriginalChart = function(param){
	am4core.ready(function() {
		am4core.options.commercialLicense = true;
		
		if( param.graph_type == "histogram" || param.graph_type == "bar" ){
			/* ----- 원본데이터 분석 그래프 Start -----------------------------------------------------------*/
			am4core.useTheme(am4themes_animated);

			// Create chart instance
			var originalDataChart = am4core.create("originalDataChartDiv", am4charts.XYChart);
			originalDataChart.scrollbarX = new am4core.Scrollbar();

			var binsMeans;
			if( "bar" == param.graph_type )
				binsMeans = param.compact_data.elements;
			else
				binsMeans = param.compact_data.bins_means;
			
			var frequency = param.compact_data.frequency;
			var list = [];
			var checkOverLength = false;
			
			for( var i in binsMeans ){
				var bin = binsMeans[i];
				if( bin.length > 20 )
					bin = bin.substring(bin.lastIndexOf(":")+1);
				
				if( bin.length > 10 )
					checkOverLength = true;					
				
				var data = {
						"범주값" : bin,
						"frequency" : frequency[i]
				}
				list.push(data);
			}
			originalDataChart.data = list;
			
			// Create axes
			var categoryAxis = originalDataChart.xAxes.push(new am4charts.CategoryAxis());
			categoryAxis.dataFields.category = "범주값";
			categoryAxis.title.text = "범주값";
			categoryAxis.renderer.grid.template.location = 0;
			categoryAxis.renderer.minGridDistance = 30;
			categoryAxis.renderer.labels.template.horizontalCenter = "right";
			categoryAxis.renderer.labels.template.verticalCenter = "middle";
			if( checkOverLength )	categoryAxis.renderer.labels.template.rotation = 270;
			else categoryAxis.renderer.labels.template.rotation = 0;
			categoryAxis.tooltip.disabled = false;
			categoryAxis.renderer.minHeight = 50;

			var valueAxis = originalDataChart.yAxes.push(new am4charts.ValueAxis());
			valueAxis.renderer.minWidth = 50;
			valueAxis.title.text = "frequency";

			// Create series
			var series = originalDataChart.series.push(new am4charts.ColumnSeries());
			series.sequencedInterpolation = true;
			series.dataFields.valueY = "frequency";
			series.dataFields.categoryX = "범주값";
			series.tooltipText = "[{categoryX}: bold]{valueY}[/]";
			series.columns.template.strokeWidth = 0;

			series.tooltip.pointerOrientation = "vertical";

			series.columns.template.column.cornerRadiusTopLeft = 10;
			series.columns.template.column.cornerRadiusTopRight = 10;
			series.columns.template.column.fillOpacity = 0.8;

			// on hover, make corner radiuses bigger
			var hoverState = series.columns.template.column.states.create("hover");
			hoverState.properties.cornerRadiusTopLeft = 0;
			hoverState.properties.cornerRadiusTopRight = 0;
			hoverState.properties.fillOpacity = 1;

			series.columns.template.adapter.add("fill", function(fill, target) {
			  return originalDataChart.colors.getIndex(target.dataItem.index);
			});

			// Cursor
			originalDataChart.cursor = new am4charts.XYCursor();
	/* ----- 원본데이터 분석 그래프 End -----------------------------------------------------------*/
		}else if( param.graph_type == "pie" ){
			am4core.useTheme(am4themes_animated);

			// Create chart instance
			var originalDataChart = am4core.create("originalDataChartDiv", am4charts.PieChart);
			originalDataChart.scrollbarX = new am4core.Scrollbar();

			
			var binsMeans = param.compact_data.elements;
			var frequency = param.compact_data.frequency;
			var list = [];
			
			for( var i in binsMeans ){
				var bin = binsMeans[i];
				if( bin.length > 15 ){
					bin = bin.substring(bin.lastIndexOf(":")+1);
					checkOverLength = true;
				}
				var data = {
						"범주값" : bin,
						"frequency" : frequency[i]
				}
				list.push(data);
			}
			originalDataChart.data = list;

			// Add and configure Series
			var pieSeries = originalDataChart.series.push(new am4charts.PieSeries());
			pieSeries.dataFields.value = "frequency";
			pieSeries.dataFields.category = "범주값";
			pieSeries.slices.template.stroke = am4core.color("#fff");
			pieSeries.slices.template.strokeWidth = 2;
			pieSeries.slices.template.strokeOpacity = 1;

			// This creates initial animation
			pieSeries.hiddenState.properties.opacity = 1;
			pieSeries.hiddenState.properties.endAngle = -90;
			pieSeries.hiddenState.properties.startAngle = -90;
		}
	}); // end am4core.ready()
}

/*Nifi, Hue 새창*/
var fnNewPage = function(type){
	var url = fngetUrlInSessionByAjax(type);
	window.open(url);
}










