function showModal(){
  var $wrap = $('.wrap');
  var $buttonModalShow = '.button__modal--show';
  var $this, $id;

  $wrap.on('click', $buttonModalShow, function(e){
    $this = $(this);
    $id = $this.attr('href');
    openModal($id, $this);
  });

  function openModal(selector, $opener) {
    var $modal = $(selector);
    var $btnClose = $modal.find('.button__modal--close');
  
    $modal.show().attr("tabindex", 0).focus();
    $btnClose.on("click.layerClose", function(){
      closeModal(selector, $opener);
    })
    $modal.on("keydown.esc", function(e){
        if (e.which === 27) {
            $btnClose.trigger("click");
        }
    });
  }
  
  function closeModal(selector, $opener) {
    var $modal = $(selector);
    var $btnClose = $modal.find('.button__modal--close');
  
    $btnClose.off("click.layerClose");
    $modal.hide().removeAttr("tabindex").off("keydown.esc");
    $opener.focus();
  }
}


function showMenu(){
  var navDepth1Active = 'nav__depth1--active',
      navDepth1       = '.nav__depth1',
      navDepth2       = '.nav__depth2',
      speed           = 300,
      $body           = $('body'),
      $navDepth1      = $('.nav__depth1'),
      $navDepth2      = $('.nav__depth2'),
      $this;
  
  $navDepth1.on('click', function(e){
    $this = $(this);
    $this.find(navDepth2).slideToggle(speed);
    $this.siblings().removeClass(navDepth1Active).find(navDepth2).slideUp(speed);
    if($this.hasClass(navDepth1Active)) return $this.removeClass(navDepth1Active);
    if($this.find('> .nav__link').hasClass('nav__button')){
      $navDepth1.removeClass(navDepth1Active);
      $this.addClass(navDepth1Active);
    }
  })

  $body.on('mouseenter', '.wrap--wide .nav__depth1', function(){
    $(this).removeClass(navDepth1Active);
  });
  $body.on('mouseleave', '.wrap--wide .nav__depth1', function(){
    $(this).removeClass(navDepth1Active)
    $navDepth2.hide();
  });
}

function showNav(){
  var $btnNavToggle = $('.button__nav--toggle'),
      $navDepth1    = $('.nav__depth1'),
      $navDepth2    = $('.nav__depth2'),
      $wrap         = $('.wrap'),
      wrapFull      = 'wrap--wide';

  $btnNavToggle.on('click', function(){
    $wrap.hasClass(wrapFull) ? closeNav() : openNav();
  });

  function closeNav(){
    $wrap.removeClass(wrapFull);
    $btnNavToggle.attr('title','?????? ??????');
    $navDepth1.off('mouseenter').off('mouseleave');
  };
  
  function openNav(){
    $wrap.addClass(wrapFull);
    $btnNavToggle.attr('title','?????? ?????????');
    $navDepth1.removeClass('nav__depth1--active');
    $navDepth2.hide();
  };
}


function uploadFile(){
  var $wrap = $('.wrap');
  var $fileList = $('.file__list');
  var $fileItem = $('.file__list > li');
  var $this, $fileName;
  
  $('.file__group [type="file"]').on('change', function(){
    $this = $(this);
    $fileName = $this.val().split('/').pop().split('\\').pop()
    if(!($this.val())) return;
    addFile($fileName)
  });
  
  $wrap.on('click', '.button__file-delete', function(e){
    deleteFile(e)
  });
  
  function addFile(newName){
    var $newFileItem = '<li class="file__item">' + newName + '<button type="button" class="button__file-delete material-icons" title="?????? ??????"><span class="hidden">?????? ??????</span></button></li>';
    if($fileItem.length <= 0) return;
    $('.file__item--none').remove();
    $fileList.append($newFileItem);
  }

  function deleteFile(e){
    if ($fileItem.length <= 0) return;
    $(e.target).parents('li').remove();  
    initFile();
  }

  function initFile(){
    $noFileItem  = '<li class="file__item file__item--none">????????? ????????? ????????????.</li>';
    if($('.file__item').length >= 1) return;
    $fileList.append($noFileItem);
  }
}


$.datepicker.setDefaults({
  dateFormat: 'yy-mm-dd',
  showOtherMonths: true,
  showMonthAfterYear:true,
  changeYear: true,
  changeMonth: true,        
  buttonText: "??????",
  yearSuffix: "???",
  monthNamesShort: ['1???','2???','3???','4???','5???','6???','7???','8???','9???','10???','11???','12???'],
  monthNames: ['1???','2???','3???','4???','5???','6???','7???','8???','9???','10???','11???','12???'],
  dayNamesMin: ['???','???','???','???','???','???','???'],
  dayNames: ['?????????','?????????','?????????','?????????','?????????','?????????','?????????'],
});


function setInputNumber() {
  var $input = $('.input__number'),
      $buttonUp = $('.button__number--up'),
      $buttonDown = $('.button__number--down'),
      $minValue = $input.attr('min'),
      // $maxValue = $input.attr('max'),
      $value, $newValue;

  
  $buttonUp.click(function() {
    $value = parseFloat($input.val());
    // if ($value >= $maxValue) return $newValue = $value;
    $newValue = $value + 1;
    $input.val($newValue).trigger('change');
  });

  $buttonDown.click(function() {
    $value = parseFloat($input.val());
    if ($value <= $minValue) return;
    $newValue = $value - 1;
    $input.val($newValue).trigger('change');
  });
}



//-----------------------------------------------------------
// tabmenu
//-----------------------------------------------------------
function setTabMenu(tabName, num){


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