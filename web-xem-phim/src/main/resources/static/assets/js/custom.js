(function ($) {

	"use strict";

	// Page loading animation
	$(window).on('load', function() {

        $('#js-preloader').addClass('loaded');

    });

	// WOW JS
	$(window).on ('load', function (){
        if ($(".wow").length) { 
            var wow = new WOW ({
                boxClass:     'wow',      // Animated element css class (default is wow)
                animateClass: 'animated', // Animation css class (default is animated)
                offset:       20,         // Distance to the element when triggering the animation (default is 0)
                mobile:       true,       // Trigger animations on mobile devices (default is true)
                live:         true,       // Act on asynchronously loaded content (default is true)
            });
            wow.init();
        }
    });

	$(window).scroll(function() {
      var scroll = $(window).scrollTop();
      // Thay thế chiều cao của header-text bằng giá trị cố định hoặc chiều cao của header
      var header = $('header').height();
      var threshold = header; // hoặc một giá trị cố định, ví dụ: 100

      if (scroll >= threshold) {
        $("header").addClass("background-header");
      } else {
        $("header").removeClass("background-header");
      }
    });

	
	$('.filters ul li').click(function(){
        $('.filters ul li').removeClass('active');
        $(this).addClass('active');
          
          var data = $(this).attr('data-filter');
          $grid.isotope({
            filter: data
          })
        });

        var $grid = $(".grid").isotope({
          	itemSelector: ".all",
          	percentPosition: true,
          	masonry: {
            columnWidth: ".all"
        }
    })

	var width = $(window).width();
		$(window).resize(function() {
			if (width > 992 && $(window).width() < 992) {
				location.reload();
			}
			else if (width < 992 && $(window).width() > 992) {
				location.reload();
			}
	})



	$(document).on("click", ".naccs .menu div", function() {
		var numberIndex = $(this).index();
	
		if (!$(this).is("active")) {
			$(".naccs .menu div").removeClass("active");
			$(".naccs ul li").removeClass("active");
	
			$(this).addClass("active");
			$(".naccs ul").find("li:eq(" + numberIndex + ")").addClass("active");
	
			var listItemHeight = $(".naccs ul")
				.find("li:eq(" + numberIndex + ")")
				.innerHeight();
			$(".naccs ul").height(listItemHeight + "px");
		}
	});

	$('.owl-features').owlCarousel({
		items:3,
		loop:true,
		dots: false,
		nav: true,
		autoplay: true,
		margin:30,
		responsive:{
			  0:{
				  items:1
			  },
			  600:{
				  items:2
			  },
			  1200:{
				  items:3
			  },
			  1800:{
				items:3
			}
		}
	})

	$('.owl-collection').owlCarousel({
		items:3,
		loop:true,
		dots: false,
		nav: true,
		autoplay: true,
		margin:30,
		responsive:{
			  0:{
				  items:1
			  },
			  800:{
				  items:2
			  },
			  1000:{
				  items:3
			}
		}
	})

	$('.owl-banner').owlCarousel({
		items:1,
		loop:true,
		dots: false,
		nav: true,
		autoplay: true,
		margin:30,
		responsive:{
			  0:{
				  items:1
			  },
			  600:{
				  items:1
			  },
			  1000:{
				  items:1
			}
		}
	})

	
	
	

	// Menu Dropdown Toggle
	if($('.menu-trigger').length){
		$(".menu-trigger").on('click', function() {
			$(this).toggleClass('active');
			$('.header-area .nav').slideToggle(200);
		});
	}


	// Menu elevator animation
	$('.scroll-to-section a[href*=\\#]:not([href=\\#])').on('click', function() {
		if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && location.hostname == this.hostname) {
			var target = $(this.hash);
			target = target.length ? target : $('[name=' + this.hash.slice(1) +']');
			if (target.length) {
				var width = $(window).width();
				if(width < 991) {
					$('.menu-trigger').removeClass('active');
					$('.header-area .nav').slideUp(200);
				}
				$('html,body').animate({
					scrollTop: (target.offset().top) - 80
				}, 700);
				return false;
			}
		}
	});

	$(document).ready(function () {
	    $(document).on("scroll", onScroll);
	    
	    //smoothscroll
	    $('.scroll-to-section a[href^="#"]').on('click', function (e) {
	        e.preventDefault();
	        $(document).off("scroll");
	        
	        $('.scroll-to-section a').each(function () {
	            $(this).removeClass('active');
	        })
	        $(this).addClass('active');
	      
	        var target = this.hash,
	        menu = target;
	       	var target = $(this.hash);
	        $('html, body').stop().animate({
	            scrollTop: (target.offset().top) - 79
	        }, 500, 'swing', function () {
	            window.location.hash = target;
	            $(document).on("scroll", onScroll);
	        });
	    });
	});

	function onScroll(event){
	    var scrollPos = $(document).scrollTop();
	    $('.nav a').each(function () {
	        var currLink = $(this);
	        var refElement = $(currLink.attr("href"));
	        if (refElement.position().top <= scrollPos && refElement.position().top + refElement.height() > scrollPos) {
	            $('.nav ul li a').removeClass("active");
	            currLink.addClass("active");
	        }
	        else{
	            currLink.removeClass("active");
	        }
	    });
	}


	// Page loading animation
	$(window).on('load', function() {
		if($('.cover').length){
			$('.cover').parallax({
				imageSrc: $('.cover').data('image'),
				zIndex: '1'
			});
		}

		$("#preloader").animate({
			'opacity': '0'
		}, 600, function(){
			setTimeout(function(){
				$("#preloader").css("visibility", "hidden").fadeOut();
			}, 300);
		});
	});

	

	const dropdownOpener = $('.main-nav ul.nav .has-sub > a');

    // Open/Close Submenus
    if (dropdownOpener.length) {
        dropdownOpener.each(function () {
            var _this = $(this);

            _this.on('tap click', function (e) {
                var thisItemParent = _this.parent('li'),
                    thisItemParentSiblingsWithDrop = thisItemParent.siblings('.has-sub');

                if (thisItemParent.hasClass('has-sub')) {
                    var submenu = thisItemParent.find('> ul.sub-menu');

                    if (submenu.is(':visible')) {
                        submenu.slideUp(450, 'easeInOutQuad');
                        thisItemParent.removeClass('is-open-sub');
                    } else {
                        thisItemParent.addClass('is-open-sub');

                        if (thisItemParentSiblingsWithDrop.length === 0) {
                            thisItemParent.find('.sub-menu').slideUp(400, 'easeInOutQuad', function () {
                                submenu.slideDown(250, 'easeInOutQuad');
                            });
                        } else {
                            thisItemParent.siblings().removeClass('is-open-sub').find('.sub-menu').slideUp(250, 'easeInOutQuad', function () {
                                submenu.slideDown(250, 'easeInOutQuad');
                            });
                        }
                    }
                }

                e.preventDefault();
            });
        });
    }


	


})(window.jQuery);
    // Hàm để bật/tắt menu dropdown
    function toggleDropdown(event) {
        event.preventDefault();
        const dropdown = document.getElementById("dropdownMenu");
        dropdown.classList.toggle("show");
    }

    // Đóng dropdown khi nhấp ra bên ngoài
    window.onclick = function(event) {
        if (!event.target.matches('#userMenu')) {
            const dropdowns = document.getElementsByClassName("dropdown-menu");
            for (let i = 0; i < dropdowns.length; i++) {
                const openDropdown = dropdowns[i];
                if (openDropdown.classList.contains('show')) {
                    openDropdown.classList.remove('show');
                }
            }
        }
    }
     $(document).ready(function(){
            $('.feature-banner').slick({
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                autoplay: true,
                autoplaySpeed: 3000,
                arrows: true,
                dots: true
            });
        });
    function logout() {
        const logoutForm = document.createElement('form');
        logoutForm.method = 'post';
        logoutForm.action = '/logout'; // Đảm bảo đường dẫn đúng với cấu hình của bạn

        // Tạo và thêm một input ẩn để bảo mật khi gửi yêu cầu POST
        const csrfToken = document.querySelector('meta[name="_csrf"]') ? document.querySelector('meta[name="_csrf"]').getAttribute('content') : '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]') ? document.querySelector('meta[name="_csrf_header"]').getAttribute('content') : '';

        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = csrfHeader; // Tên header CSRF
        input.value = csrfToken; // Giá trị token CSRF

        logoutForm.appendChild(input);
        document.body.appendChild(logoutForm);
        logoutForm.submit(); // Gửi yêu cầu POST
    }
     document.addEventListener('DOMContentLoaded', function () {
      const replyButtons = document.querySelectorAll('.reply-btn');

      replyButtons.forEach(button => {
        button.addEventListener('click', function () {
          const replyForm = this.parentElement.parentElement.querySelector('.reply-form');
          replyForm.classList.toggle('d-none');
        });
      });
    });

        function highlightStars(rating) {
         const stars = document.querySelectorAll('.rating label');
         stars.forEach((star, index) => {
             if (index < rating) {
                 star.classList.add('star-filled'); // Sáng
             } else {
                 star.classList.remove('star-filled'); // Tắt
             }
         });
     }

     function resetStars(averageRating) {
         const stars = document.querySelectorAll('.rating label');

         // Xóa tất cả các lớp star-filled
         stars.forEach(star => {
             star.classList.remove('star-filled');
         });

         // Hiển thị lại sao dựa trên điểm trung bình
         const selectedRating = document.querySelector('input[name="rating"]:checked');
         if (selectedRating) {
             const ratingValue = parseInt(selectedRating.value);
             highlightStars(ratingValue); // Hiển thị sao theo rating đã chọn
         } else {
             highlightStars(Math.round(averageRating)); // Hiển thị sao theo điểm trung bình
         }
     }

     function submitRating(rating) {
         document.querySelector('input[name="rating"][value="' + rating + '"]').checked = true; // Chọn rating
         document.getElementById('ratingForm').submit(); // Gửi form
     }
      // Đợi trang tải xong
         document.addEventListener("DOMContentLoaded", function() {
             // Tự động ẩn thông báo sau 5 giây
             setTimeout(function() {
                 var errorAlert = document.getElementById('errorAlert');
                 var successAlert = document.getElementById('successAlert');
                 if (errorAlert) {
                     errorAlert.style.display = 'none';
                 }
                 if (successAlert) {
                     successAlert.style.display = 'none';
                 }
             }, 3000); // 5000 ms = 5 giây
         });
//function liveSearch() {
//    const query = document.getElementById('searchText').value;
//
//    if (query.length < 3) {
//        // Nếu từ khóa tìm kiếm ít hơn 3 ký tự, không thực hiện tìm kiếm
//        document.getElementById('searchResults').style.display = 'none';
//        return;
//    }
//
//    // Gửi yêu cầu AJAX đến server
//    const xhr = new XMLHttpRequest();
//    xhr.open('GET', '/livesearch?keyword=' + query, true);  // Đảm bảo đúng URL để nhận dữ liệu từ controller
//    xhr.setRequestHeader('Content-Type', 'application/json');
//
//    xhr.onload = function() {
//        if (xhr.status === 200) {
//            const results = JSON.parse(xhr.responseText);  // Giả sử bạn trả về kết quả dưới dạng JSON
//
//            console.log("Results from server:", results);  // Log kết quả trả về từ server
//
//            // Hiển thị kết quả tìm kiếm
//            const resultsContainer = document.getElementById('searchResults');
//            if (results.length > 0) {
//                let resultHTML = '';
//                results.forEach(result => {
//                    // Tạo đường dẫn cho phim tùy thuộc vào isSeries
//                    const movieLink = result.isSeries ?
//                        `/phimbo/${result.slug}` :
//                        `/phimle/${result.slug}`;
//
//                    // Tạo HTML cho mỗi kết quả tìm kiếm
//                    resultHTML += `
//
//                    <ul style="list-style: none; padding: 0;">
//                        <li style="border-bottom: 1px solid #7b7b7b; padding: 5px 5px;">
//                            <a href="${movieLink}" style="display: flex; align-items: center; text-decoration: none; color: inherit;">
//                                <img src="${result.posterUrl}" alt="" style="max-width: 46px; height: 100%; width:100%; border-radius: 5px;">
//                                <div style="flex-grow: 1; margin-left: 5px">
//                                    <h6 style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 280px;">
//                                        ${result.title}
//                                    </h6>
//                                     <p style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 280px;">
//                                                ${result.name} - ${result.releaseYear}
//                                     </p>
//                                     <div style="font-size: 0.9em; color: gray; display: flex; justify-content: space-between; align-items: center;">
//                                                 <div style="display: flex; align-items: center;">
//                                                     <i class="bi bi-calendar-range" style="margin-right: 5px;"></i>
//                                                     <span>${result.releaseYear}</span>
//                                                 </div>
//                                             </div>
//                                </div>
//                            </a>
//                        </li>
//                    </ul>
//
//                    `;
//                });
//                resultsContainer.innerHTML = resultHTML;
//                resultsContainer.style.display = 'block';
//            } else {
//                resultsContainer.innerHTML = 'No results found';
//                resultsContainer.style.display = 'block';
//            }
//        } else {
//            console.error('Request failed with status:', xhr.status);
//        }
//    };
//
//    xhr.send();
//}
function liveELSearch() {
    const query = document.getElementById('searchText').value;

    if (query.length < 3) {
        // Nếu từ khóa tìm kiếm ít hơn 3 ký tự, không thực hiện tìm kiếm
        document.getElementById('searchResults').style.display = 'none';
        return;
    }

    // Gửi yêu cầu AJAX đến server
    const xhr = new XMLHttpRequest();
    xhr.open('GET', '/live?keyword=' + query, true);  // Đảm bảo đúng URL để nhận dữ liệu từ controller
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.onload = function() {
        if (xhr.status === 200) {
            const results = JSON.parse(xhr.responseText);  // Giả sử bạn trả về kết quả dưới dạng JSON

            console.log("Results from server:", results);  // Log kết quả trả về từ server

            // Hiển thị kết quả tìm kiếm
            const resultsContainer = document.getElementById('searchResults');
            if (results.length > 0) {
                let resultHTML = '';
                results.forEach(result => {
                    // Tạo đường dẫn cho phim tùy thuộc vào isSeries
                    const movieLink = result.isSeries ?
                        `/phimbo/${result.slug}` :
                        `/phimle/${result.slug}`;

                    // Tạo HTML cho mỗi kết quả tìm kiếm
                    resultHTML += `

                    <ul style="list-style: none; padding: 0;">
                        <li style="border-bottom: 1px solid #7b7b7b; padding: 5px 5px;">
                            <a href="${movieLink}" style="display: flex; align-items: center; text-decoration: none; color: inherit;">
                                <img src="${result.posterUrl}" alt="" style="max-width: 46px; height: 100%; width:100%; border-radius: 5px;">
                                <div style="flex-grow: 1; margin-left: 5px">
                                    <h6 style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 260px;">
                                        ${result.title}
                                    </h6>
                                     <p style=" white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 260px;">
                                                ${result.name} - ${result.releaseYear}
                                     </p>
                                     <div style="font-size: 0.9em; color: gray; display: flex; justify-content: space-between; align-items: center;">
                                                 <div style="display: flex; align-items: center;">
                                                     <i class="bi bi-calendar-range" style="margin-right: 5px;"></i>
                                                     <span>${result.releaseYear}</span>
                                                 </div>
                                             </div>
                                </div>
                            </a>
                        </li>
                    </ul>

                    `;
                });
                resultsContainer.innerHTML = resultHTML;
                resultsContainer.style.display = 'block';
            } else {
                resultsContainer.innerHTML = 'No results found';
                resultsContainer.style.display = 'block';
            }
        } else {
            console.error('Request failed with status:', xhr.status);
        }
    };

    xhr.send();
}
document.addEventListener('click', function(event) {
    // Lấy phần tử chứa tìm kiếm và kết quả
    const searchContainer = document.getElementById('searchContainer');
    const searchResults = document.getElementById('searchResults');

    // Kiểm tra nếu click xảy ra bên ngoài vùng .search-input
    if (!searchContainer.contains(event.target)) {
        searchResults.style.display = 'none'; // Ẩn kết quả tìm kiếm
    }
});
document.addEventListener("DOMContentLoaded", function () {
    const swiper = new Swiper(".swiper-container", {
        loop: true, // Chạy vòng lặp
        autoplay: {
            delay: 3000, // Tự động chạy slide mỗi 3 giây
            disableOnInteraction: false, // Không dừng khi tương tác
        },
        slidesPerView: 1, // Luôn chỉ hiển thị 1 slide
        spaceBetween: 0, // Không có khoảng cách giữa các slide
        centeredSlides: false, // Tắt căn giữa để chỉ 1 slide hiển thị
    });

    // Xử lý nút đóng banner
    document.getElementById("close-banner").addEventListener("click", function () {
        document.getElementById("banner-bottom").style.display = "none";
    });
});


