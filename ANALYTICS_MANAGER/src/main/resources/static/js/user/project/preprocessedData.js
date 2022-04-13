var preprocessedIntervalId = 0;
$(function(){
	
	// 전처리 리스트 클릭시
	$(document).on("click", ".preprocessedData", function(){
		/*샌드박스 파일브라우저 샘플 미리보기*/
		$(".preprocessedData").removeClass("activeSel");
		$(this).addClass("activeSel");
		selectedPreprocessedDataPk = $(this).attr("id");
		fnGetPreprocessedData();
	});
	
	/*전처리데이터 차트 변경시*/
	$(document).on("change", "#preprocessedDataChartName", function(){
		fnPreprocessedDataChart(JSON.parse($(this).val()));
	});
	
	/*학습 모달 탭 클릭시*/
	$(document).on("click", "#analysis-tab", function(){
		// 학습 모달 분석정보 확인
		fnAnalysisConfirm();
	});
	
	/*학습 모달 알고리즘 조회*/
	$(document).on("click", "#searchAlgorithm", function(){
		fnSearchAlgorithm();
	});
	// searchAlgorithmValue
	$("#searchAlgorithmValue").bind("keypress", function(e){
		if( e.which == 13 )	fnSearchAlgorithm();
	});
	
	/*학습 모달 알고리즘 선택시*/
	$(document).on("click", ".algorithm", function(){
		$(".algorithm").removeClass("active");
		$(this).addClass("active");
		/*파라미터 설정*/
		fnSetAlgorithmParams(JSON.parse($(this).attr("data-algorithm")));
	});
	
	/*PREV버튼 클릭 */ //fnSetTabMenu(tabName, num)
	$(document).on("click", "#prevBtn", function(){
		$("#study_tab").find("li").each(function(){
			if( $(this).hasClass("on") ){
				if( "parameter-tab" == $(this).children().attr("id") ){
					fnSetTabMenu("#study_tab", "0");
					return false;
				}else if( "analysis-tab" == $(this).children().attr("id") ){
					fnSetTabMenu("#study_tab", "1");
					return false;
				}
			}
		});
		
	});
	
	/*NEXT버튼 클릭 */
	$(document).on("click", "#nextBtn", function(){
		$("#study_tab").find("li").each(function(){
			if( $(this).hasClass("on") ){
				if( "algorithm-tab" == $(this).children().attr("id") ){
					fnSetTabMenu("#study_tab", "1");
					return false;
				}else if( "parameter-tab" == $(this).children().attr("id") ){
					fnSetTabMenu("#study_tab", "2");
					return false;
				}
			}
		});
	});
	
});

/*전처리 목록 가져오기*/
var fnGetPreprocessedDataList = function(){
	var html = "";
	if( fnNotNullAndEmpty(selectedOriginalDataPk) ){
		/*전처리 리스트 가져오기*/
		var preprocessedDataList = fnGetPreprocessedDataListByAjax(selectedInstancePk, selectedOriginalDataPk);
		for( var i in preprocessedDataList ){
			var pData = preprocessedDataList[i];
			if( i == 0 ){
				html += "<li class='preprocessedData activeSel' id="+pData.PREPROCESSED_DATA_SEQUENCE_PK+"><a href='javascript:'>"+pData.NAME+"</a></li>";
				
				selectedPreprocessedDataPk = pData.PREPROCESSED_DATA_SEQUENCE_PK;
				fnGetPreprocessedData(pData);/* 전처리데이터 가져오기*/
				
			}else{
				html += "<li class='preprocessedData' id="+pData.PREPROCESSED_DATA_SEQUENCE_PK+"><a href='javascript:'>"+pData.NAME+"</a></li>";
			}
		}
	}
	
	if( html == "" ){
		html += "<li>전처리 데이터가 없습니다.</li>"
		selectedPreprocessedDataPk = ""; 
		$("#preprocessedDiv").hide(); // 전처리 숨김
		
		// 모델 리스트 가져오기
		fnGetModelList();
		
		// 종료
		if( preprocessedIntervalId != 0 )	clearInterval(preprocessedIntervalId);
		
		$("#loading").hide();
	}else{
		$("#deletePreprocessedDataBtn").show();
		$("#preprocessedDiv").fadeIn(); // 전처리 숨김
		
	}
	$("#preprocessedDataList").html(html);
	$("#loading").hide();
}

/* 전처리데이터 가져오기*/
var fnGetPreprocessedData = function(pData){
	if( pData == null ){
		pData = fnGetPreprocessedDataByAjax(selectedInstancePk, selectedOriginalDataPk, selectedPreprocessedDataPk);
	}
	
	$(".pDataName").text(pData.NAME);
	$("#preprocessedDetailDiv").hide();
	$("#preprocessedChartDiv").hide();
	$("#learningModalBtn").hide();
	if( preprocessedIntervalId != 0 )	clearInterval(preprocessedIntervalId);
	
	/* 전처리 상태값 체크 후 변경 */
	if( "ongoing" == pData.PROGRESS_STATE ){
		var html = "";
		html += "	<div class='progress' style='margin-bottom:0px;'>" +
		"				<div class='progress-bar progress-bar-striped active' role='progressbar' style='width:100%'>생성중</div></div>";
		
		$("#pDataFilename").html(html);
		$("#pDataFilepath").html(html);
		$("#pDataAmount").html(html);
		$("#pDataCreateDatetime").text(pData.CREATE_DATETIME.substring(0,19).replace("T"," "));
		
		preprocessedIntervalId = setInterval(fnChangePreprocessedState, 5000);
		
		// 모델 리스트 가져오기
		fnGetModelList();
		
	}else if( "fail" == pData.PROGRESS_STATE ){
		$("#pDataFilename").html("실패");
		$("#pDataFilepath").html("실패");
		$("#pDataAmount").html("0");
		$("#pDataCreateDatetime").text(pData.CREATE_DATETIME.substring(0,19).replace("T"," "));
		
		// 모델 리스트 가져오기
		fnGetModelList();
		
	}else if( "success" == pData.PROGRESS_STATE ){
		$("#pDataFilename").text(pData.FILENAME);
		$("#pDataFilepath").text(pData.FILEPATH);
		$("#pDataAmount").text(numberWithCommas(pData.AMOUNT));
		$("#pDataCreateDatetime").text(pData.CREATE_DATETIME.substring(0,19).replace("T"," "));
		$("#preprocessedDetailDiv").fadeIn();
		$("#preprocessedChartDiv").fadeIn();
		$("#learningModalBtn").show();
		$("#preprocessedDiv").fadeIn();
		
		// 전처리 프로세스 생성
		fnSetRequestData(pData.COMMAND.request_data);
		
		// 차트 설정
		fnSetStatistics(pData.STATISTICS);
		
		// 모델 리스트 가져오기
		fnGetModelList();
		
	}else{
		$("#pDataFilename").html(pData.PROGRESS_STATE);
		$("#pDataFilepath").html(pData.PROGRESS_STATE);
		$("#pDataAmount").html(pData.PROGRESS_STATE);
		$("#pDataCreateDatetime").text(pData.CREATE_DATETIME.substring(0,19).replace("T"," "));
		
		// 모델 리스트 가져오기
		fnGetModelList();
	}
}

/*차트 설정*/
var fnSetStatistics = function(statistics){
	var html = "";
	for( var i in statistics ){
		if( "datetime" != statistics[i].name ){
			if( html == "" )	fnPreprocessedDataChart(statistics[i]);
			html += "<option value='"+JSON.stringify(statistics[i])+"'>"+statistics[i].name+"</option>";
		}
	}
	$("#preprocessedDataChartName").html(html);
}

/*전처리 프로세스 생성*/
var fnSetRequestData = function(requestData){
	// 전처리 함수 가져오기
	var preprocessFunctionList = fnGetPreprocessFunctionByAjax();
	var html = "";
	var functionName = "";
	for( var i in requestData ){
		html += "<tr><td>"+requestData[i].field_name+"</td>"
		for( var f in preprocessFunctionList ){
			if( requestData[i].preprocess_functions_sequence_pk == preprocessFunctionList[f].PREPROCESS_FUNCTION_SEQUENCE_PK )
				functionName = preprocessFunctionList[f].PREPROCESS_FUNCTION_NAME;
		}
		html += "<td>"+functionName+"</td>"
		if( fnNotNullAndEmpty(requestData[i].condition) ){
			var condition = requestData[i].condition;
			var cnt = 0;
			$.each(condition, function(index, value){
				cnt++;
				if( cnt == 1 ){
					html += "<td>"+index+"</td>";
					html += "<td>"+value+"</td></tr>";	
				}else{
					html += "<tr><td>"+requestData[i].field_name+"</td>";
					html += "<td>"+functionName+"</td>";
					html += "<td>"+index+"</td>";
					html += "<td>"+value+"</td></tr>";
				}
			});
		}else{
			html += "<td>없음</td>";
			html += "<td></td></tr>";
		}
	}
	
	$("#requestDataTbody").html(html);
	
}

/*전처리셋 상태값 변경*/
var fnChangePreprocessedState = function(){
	var pData = fnGetPreprocessedDataByAjax(selectedInstancePk, selectedOriginalDataPk, selectedPreprocessedDataPk);
	if( "success" == pData.PROGRESS_STATE ){
		$("#pDataFilename").text(pData.FILENAME);
		$("#pDataFilepath").text(pData.FILEPATH);
		$("#pDataAmount").text(numberWithCommas(pData.AMOUNT));
		$("#preprocessedDetailDiv").fadeIn();
		$("#preprocessedChartDiv").fadeIn();
		$("#learningModalBtn").fadeIn();
		
		// 전처리 프로세스 생성
		fnSetRequestData(pData.COMMAND.request_data);
		// 차트 설정
		fnSetStatistics(pData.STATISTICS);
		// 종료
		clearInterval(preprocessedIntervalId);
		
	}else if( "fail" == pData.PROGRESS_STATE ){
		$("#pDataFilename").text("실패");
		$("#pDataFilepath").text("실패");
		$("#pDataAmount").text("실패");
		$("#preprocessedDetailDiv").hide();
		$("#preprocessedChartDiv").hide();
		$("#learningModalBtn").hide();
		fnComNotify("error", "전처리 생성을 실패 하였습니다. 관리자에게 문의해주세요.");
		clearInterval(preprocessedIntervalId);
	}
}

/*전처리 삭제*/
var fnDeletePreprocessedData = function(){
	if( confirm("전처리 데이터를 삭제하면 생성된 모델이 같이 삭제됩니다. \n삭제하시겠습니까?") ){
		var result = fnDeletePreprocessedDataByAjax(projectSequencePk, selectedPreprocessedDataPk);
		if( result == "success" ){
			fnComNotify("success", "전처리 데이터를 삭제 하였습니다.");
			// 전처리 리스트 가져오기
			fnGetPreprocessedDataList();
		}		
	}
}


/*학습 모달 알고리즘 조회*/
var fnSearchAlgorithm = function(){
	fnSetTabMenu('#study_tab','0');
//	$('a[href="#tab_algorithm"]').click();
	var algorithms = fnSearchAlgorithmByAjax($("#searchAlgorithmValue").val());
	var html = "";
	for( var i in algorithms ){
		if( i == 0 ){
			html += "<li class='algorithm active' data-algorithm='"+JSON.stringify(algorithms[i])+"' role='button'>" +
					"<a>"+algorithms[i].LIBRARY_FUNCTION_NAME+"</a></li>";
			// 파라미터 설정
			fnSetAlgorithmParams(algorithms[i]);
		}else{
			html += "<li class='algorithm' data-algorithm='"+JSON.stringify(algorithms[i])+"' role='button'>" +
					"<a>"+algorithms[i].LIBRARY_FUNCTION_NAME+"</a></li>";
		}
	}
	if( html == "" ) html = "<li>검색 결과가 없습니다.</li>";

	$("#searchAlgorithmList").html(html);
	N2M.ui.toggleModal('#learningModal');
}
	
/*파라미터 설정*/
var fnSetAlgorithmParams = function(algorithm){
	// 알고리즘 선택 결과
	var result = "";
	result += "Algorithm: "+algorithm.ALGORITHM_NAME+"\nLibrary: "+algorithm.LIBRARY_NAME
		   +"\nUsage: "+algorithm.LIBRARY_FUNCTION_USAGE+"\nInfomation: "+algorithm.LIBRARY_FUNCTION_DESCRIPTION;
	
	$("#selectedAlgorithmResult").text(result);
	
	// 모델 파라미터 설정
	var modelParams = algorithm.MODEL_PARAMETERS;
	var mParamHtml = "";
	for( var i in modelParams ){
		var mp = modelParams[i];
		mParamHtml += "<tr>";
		mParamHtml += "	<td>"+mp.name+"</td>";
		mParamHtml += "	<td>"+mp.type+"</td>";
		mParamHtml += "	<td>"+mp.default+"</td>";
		if( mp.type == "int" )
			mParamHtml += "	<td><input type='text' class='form-control' onkeydown='return fnOnlyNumber();' onkeyup='fnRemoveChar(event);' placeholder='"+mp.default+"' ></td>";
		else if( mp.type == "float" )
			mParamHtml += "	<td><input type='text' class='form-control' onkeydown='return fnOnlyNumberDot();' onkeyup='fnRemoveChar(event);' placeholder='"+mp.default+"' ></td>";
		else
			mParamHtml += "	<td><input type='text' class='form-control' placeholder='"+mp.default+"' ></td>";
		
		mParamHtml += "</tr>";
	}
	$("#modelParams").html(mParamHtml);
	
	// 학습 파라미터 설정
	var trainParams = algorithm.TRAIN_PARAMETERS;
	var tParamHtml = "";
	var tParamY = "<select class='select'>";
	$("#originalDataChartName option").each(function(i){
		tParamY += "<option value="+$(this).text()+">"+$(this).text()+"</option>";
	});
	tParamY += "</select>";
	for( var i in trainParams ){
		var tp = trainParams[i];
		tParamHtml += "<tr>";
		tParamHtml += "	<td>"+tp.name+"</td>";
		tParamHtml += "	<td>"+tp.type+"</td>";
		tParamHtml += "	<td>"+tp.default+"</td>";
		if( "y" == tp.name )
			tParamHtml += "	<td>"+tParamY+"</td>";
		else
			tParamHtml += "	<td><input type='text' class='form-control' onkeyup='fnRemoveChar(event);' placeholder='"+tp.default+"'></td>";
		
		tParamHtml += "</tr>";
	}
	$("#trainParams").html(tParamHtml);
	
	// 분석정보확인 탭 설정
	$("#libraryName").text(algorithm.LIBRARY_NAME);
	$("#algorithmName").text(algorithm.ALGORITHM_NAME);
}

//학습 모달 분석정보 확인
var fnAnalysisConfirm = function(tabId){
	var modelParameters = {};
	var trainParameters = {};
	$("#modelParams tr").each(function(i){
		var tr = $(this);
		var pName = tr.children().eq(0).text();
		var pDefault = tr.children().eq(2).text();
		var pValue = tr.children().children().val();
		
		if( fnNotNullAndEmpty(pValue) ){
			modelParameters[pName] = pValue;
		}else{
			modelParameters[pName] = pDefault;
		}
	});
	
	$("#trainParams tr").each(function(){
		var tr = $(this);
		var tName = tr.children().eq(0).text();
		var tDefault = tr.children().eq(2).text();
		var tValue = tr.children().children().val();
		
		if( fnNotNullAndEmpty(tValue) ){
			trainParameters[tName] = tValue;
		}else{
			trainParameters[tName] = tDefault;
		}
	});
	$("#algorithmModelParameters").text(JSON.stringify(modelParameters,null,2));
	$("#algorithmTrainParameters").text(JSON.stringify(trainParameters,null,2));
}

/*모델 생성*/
var fnCreateModel = function(){
	// 알고리즘 algorithms_sequence_pk
	var data = {};
	var trainData = {};
	var modelParameters = {};
	var trainParameters = {};
	var algorithmsSequencePk;
	$("#searchAlgorithmList li").each(function(){
		var algorithm;
		if( $(this).hasClass("active") ){
			algorithm = JSON.parse($(this).attr("data-algorithm"));
			algorithmsSequencePk = algorithm.ALGORITHM_SEQUENCE_PK;
		}
	});
	if( fnNotNullAndEmpty(algorithmsSequencePk) ){
		data["algorithms_sequence_pk"] = algorithmsSequencePk;
		// train_data {preprocessed_data_sequence_pk : }
		trainData["preprocessed_data_sequence_pk"] = selectedPreprocessedDataPk;
		data["train_data"] = trainData;
		
		// model_parameters
		$("#modelParams tr").each(function(i){
			var tr = $(this);
			var pName = tr.children().eq(0).text();
			var pValue = tr.children().children().val();
			
			if( fnNotNullAndEmpty(pValue) ){
				modelParameters[pName] = pValue;
			}
		});
		data["model_parameters"] = modelParameters;
		
		// train_parameters	
		$("#trainParams tr").each(function(){
			var tr = $(this);
			var tName = tr.children().eq(0).text();
			var tValue = tr.children().children().val();
			
			if( fnNotNullAndEmpty(tValue) ){
				trainParameters[tName] = tValue;
			}
		});
		data["train_parameters"] = trainParameters;
		
		if( confirm("학습을 시작하시겠습니까?") ){
			var response = fnModelsByAjax(projectSequencePk, data);
			if( response.result == "success" ){
				// 모델 리스트 조회
				fnGetModelList();
				fnN2MCloseModal("#learningModal");
				fnComNotify("success", "모델 생성요청을 하였습니다.");
				
			}else if( fnNotNullAndEmpty(response.detail) ){
				if( fnNotNullAndEmpty(response.data) )
					fnComErrorMessage(response.detail, response.data);
				else fnComNotify("error", response.detail);
			}
		}
	}else{
		fnComNotify("warning", "알고리즘을 선택해주세요.");
	}
}

/*전처리 차트*/
var fnPreprocessedDataChart = function(param){
	am4core.ready(function() {
		am4core.options.commercialLicense = true;
		
		if( param.graph_type == "histogram" || param.graph_type == "bar" ){
			// Themes begin
			am4core.useTheme(am4themes_animated);
			// Themes end

			// Create chart instance
			var preprocessedDataChart = am4core.create("preprocessedDataChartDiv", am4charts.XYChart);
			preprocessedDataChart.scrollbarX = new am4core.Scrollbar();

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
						"범주값" : binsMeans[i],
						"frequency" : frequency[i]
				}
				list.push(data);
			}
			preprocessedDataChart.data = list;
			
			// Create axes
			var categoryAxis = preprocessedDataChart.xAxes.push(new am4charts.CategoryAxis());
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

			var valueAxis = preprocessedDataChart.yAxes.push(new am4charts.ValueAxis());
			valueAxis.renderer.minWidth = 50;
			valueAxis.title.text = "frequency";

			// Create series
			var series = preprocessedDataChart.series.push(new am4charts.ColumnSeries());
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
			  return preprocessedDataChart.colors.getIndex(target.dataItem.index);
			});

			// Cursor
			preprocessedDataChart.cursor = new am4charts.XYCursor();
			
		}else if( param.graph_type == "pie" ){
			// Themes begin
			am4core.useTheme(am4themes_animated);
			// Themes end

			// Create chart instance
			var preprocessedDataChart = am4core.create("preprocessedDataChartDiv", am4charts.PieChart);
			preprocessedDataChart.scrollbarX = new am4core.Scrollbar();

			
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
						"범주값" : binsMeans[i],
						"frequency" : frequency[i]
				}
				list.push(data);
			}
			preprocessedDataChart.data = list;

			// Add and configure Series
			var pieSeries = preprocessedDataChart.series.push(new am4charts.PieSeries());
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