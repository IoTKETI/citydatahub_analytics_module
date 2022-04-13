var N2M = N2M || {};

N2M.ui = (function() {

  // element
  var $allmenu                  = $('.all-menu'),
      $html                     = $('html'),
      $nav                      = $('.nav'),
      $navDepth1List            = $('.nav-depth1__list'),
      $navDepth1Link            = $('.nav-depth1__link'),
      $navDepth1Item            = $('.nav-depth1__item'),
      $navDepth2List            = $('.nav-depth2__list'),
      $navBG                    = $('.nav__bg'),
      $footerTopButton          = $('.footer-top-button'),
      $footerDropdownMenu       = $('.footer-dropdown-menu'),
      $footerDropdownMenuButton = $('.footer-dropdown-menu__button'),
      $categoryNav              = $('.category-nav'),
      $categoryNavItem          = $('.category-nav__item'),
      $window                   = $(window);

	// [공통] nav's mouseenter handler 
  function onEnterNav() {
    $nav.addClass('is-active');
    $(this).addClass('is-active').siblings().removeClass('is-active');
  }

	// [공통] nav's mouseleave handler
  function onLeaveNav() {
    $nav.removeClass('is-active');
    $(this).removeClass('is-active');
  }

	// [공통] nav-depth2-list 중앙 정렬
  function setNavDepth2ListPositionCenter(){
    var $navDepth2List = $('.nav-depth2__list');
    var $navDepth1ItemWidth = $navDepth1List.outerWidth() / $navDepth1Item.length;
    console.log($navDepth1ItemWidth);
    
    $navDepth2List.each(function(index, item){
      var biggerWidth = 0;
  
      function getBiggerWidth(children){
        $(item).find(children).each(function(index, item) {
          var currentItemWidth = $(item).width();
          if(biggerWidth <= currentItemWidth){
            biggerWidth = currentItemWidth;
          }
        });
        $(item).children().css({
          marginLeft: ($navDepth1ItemWidth - biggerWidth) / 2
        });
      }
  
      if($(item).has('.nav-depth3__list').length > 0){
        getBiggerWidth('.nav-depth3__link');
      }else{
        getBiggerWidth('.nav-depth2__link');
      }
    });
  }

  // [공통] footer-dropdown-menu toggle
	function toggleFooterDropdownMenu(){
		$footerDropdownMenu.toggleClass('is-active');
  }
  
  // [공통] footerTopButton 위치 지정
  function fixedScroll(){
    var $footerTopButton   = $(".footer-top-button"),
        speed              = 300,
        scrollTopNav       = 40,
        scrollBottomFooter = $(document).height() - $window.height() - ($('.footer').height() / 2),
        timer, 
        $this;

    if(!timer) {
      timer = setTimeout(function(){
        timer = null;
        if($window.scrollTop() > scrollTopNav) {
          $footerTopButton.addClass('is-show');
        } else {
          $footerTopButton.removeClass('is-show');
        }

        if($window.scrollTop() >= scrollBottomFooter){
          $footerTopButton.addClass('is-fixed');
        }else{
          $footerTopButton.removeClass('is-fixed');
        }
      }, 50);
    }
  }

  // [공통] footerTopButton 클릭하면 페이지 상단으로 이동
  function moveTop() {
    $('html, body').animate({ scrollTop: 0 }, 100);
  }
  
  // [서브 공통] sub-nav-depth2__list toggle
  function toggleSubNavDepth2List(){
    var $subNavDepth1ItemIsOnlyDepth1 = $('.sub-nav-depth1__item.is-only-depth1');
    var $subNavDepth1ItemIsActive     = $('.sub-nav-depth1__item.is-active');

    $subNavDepth1ItemIsOnlyDepth1.on('mouseenter', function(){
      $subNavDepth1ItemIsActive.children('.sub-nav-depth2__list').css({visibility:'hidden'});
    });
    $subNavDepth1ItemIsOnlyDepth1.on('mouseleave', function(){
      $subNavDepth1ItemIsActive.children('.sub-nav-depth2__list').css({visibility:'visible'});
    });
  }

  // [모바일] allmenu toggle
  $allmenu.on('click', function(){
    if($html.hasClass('is-mobile-nav-show')) {
      $html.removeAttr('class');
      $allmenu.find('span').text('전체 메뉴 닫기');
    }else {
      $html.removeAttr('class');
      $html.addClass('is-mobile-nav-show is-scroll-blocking');
      $allmenu.find('span').text('전체 메뉴 열기');
    }
  });

  // [모바일] search toggle
  function toggleSearch(){
    var $headerSearchField  = $('.header-search__field'),
        $headerSearchSubmit = $('.header-search__submit'),
        $headerSearchClose  = $('.header-search__close');

    $headerSearchSubmit.on('click', function(){
      if($(this).parents($html).hasClass('is-mobile-search-show')) return;
      $html.removeAttr('class');
      $html.addClass('is-scroll-blocking is-mobile-search-show');
    });
    $headerSearchClose.on('click', function(){
      $html.removeAttr('class');
    });
  }

	// [서브] sub category-nav toggle
  function toggleCategoryNavHandler(){
    if(this === $categoryNavItem[0]){
      if($('.category-nav__item.is-select').length < $categoryNavItem.length - 1){
        $(this).siblings().addClass('is-select');
      }else if($('.category-nav__item.is-select').length === $categoryNavItem.length - 1){
        $(this).siblings().removeClass('is-select');
      }
    } else if(this !== $categoryNavItem[0]) {
      $(this).toggleClass('is-select');
    }
  }

  // 관심상품, 이용신청하기 버튼 토글
  function toggleDataUnitButton(){
    var $subContent = $('.sub__content');
    $subContent.on('click', '.data-unit__button', function(){
      $(this).toggleClass('is-active');
    });
  }

	// [데이트피커] datepicker's options
  $.datepicker.setDefaults({
    dateFormat         : 'yy-mm-dd',
    showOtherMonths    : true,
    showMonthAfterYear : true,
    changeYear         : true,
    changeMonth        : true,
    buttonText         : "선택",
    yearSuffix         : "년",
    monthNamesShort    : ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
    monthNames         : ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
    dayNamesMin        : ['일','월','화','수','목','금','토'],
    dayNames           : ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
  });

  // [반응형] window의 너비에 따라 조건문 실행
  function getWindowWidth(){
    var $windowWidth = $window.outerWidth();
    if($windowWidth >= 1260){
      setNavDepth2ListPositionCenter();
      toggleSubNavDepth2List();
      $nav.on('mouseenter', '.nav-depth1__item', onEnterNav);
      $nav.on('mouseleave', '.nav-depth1__item', onLeaveNav);
    } else {
      $nav.off('mouseenter');
      $nav.off('mouseleave');
    } 
    if($windowWidth <= 1024){
      toggleSearch();
    } 
  }

  // 문서를 처음 불러왔을 때 실행
  $(document).ready(function(){
    getWindowWidth();
  });

  // resize 될 때 마다 실행
  $(window).resize(function(){
    getWindowWidth();
  });

  // eventListner
  $footerTopButton.on('click', moveTop);
  $footerDropdownMenuButton.on('click', toggleFooterDropdownMenu);
  $categoryNav.on('click', '.category-nav__item', toggleCategoryNavHandler);
  $window.on('scroll', fixedScroll);
  $nav.on('click', '.nav-depth1__item', function(){
    if($window.outerWidth() > 640) return;
    $(this).toggleClass('is-active').siblings().removeClass('is-active');
  });

  return {
    toggleDataUnitButton : toggleDataUnitButton,
  };
})();
