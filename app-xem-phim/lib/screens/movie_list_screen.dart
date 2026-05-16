import 'package:flutter/material.dart';
import '../services/movie_service.dart';
import 'filter_movies_screen.dart';
import 'movie_search.dart';
import 'filter_menu.dart';
import 'movie_detail.dart';
import 'dart:async';
import 'login_screen.dart';
class MovieListScreen extends StatefulWidget {
  @override
  _MovieListScreenState createState() => _MovieListScreenState();
}

class _MovieListScreenState extends State<MovieListScreen> {
  late Future<Map<String, List<dynamic>>> categorizedMoviesFuture;
  late Future<List<Map<String, dynamic>>> topRatedMoviesFuture;

  String? _selectedCategory;
  String? _selectedSort;
  int? _selectedGenre;
  int? _selectedCountry;
  int? _selectedYear;

  @override
  void initState() {
    super.initState();
    categorizedMoviesFuture = MovieService.fetchCategorizedMovies();
    topRatedMoviesFuture = MovieService.getTopRatedMovies();
  }

  Future<void> _refreshMovies() async {
    setState(() {
      categorizedMoviesFuture = MovieService.fetchCategorizedMovies();
      topRatedMoviesFuture = MovieService.getTopRatedMovies();
    });
  }

  Future<void> _showFilterMenu() async {
    Navigator.push(
      context,
      MaterialPageRoute(
        fullscreenDialog: true, // Sử dụng fullscreen modal với AppBar
        builder: (BuildContext context) {
          return Scaffold(
            appBar: AppBar(
              title: Text(
                'Lọc Phim',
                style: TextStyle(color: Colors.white),
              ),
              backgroundColor: Colors.black,
              centerTitle: true,
              actions: [
                IconButton(
                  icon: Icon(Icons.close, color: Colors.white),
                  onPressed: () {
                    Navigator.pop(context); // Đóng FilterMenu
                  },
                ),
              ],
            ),
            backgroundColor: Colors.black, // Màu nền của Scaffold
            body: Padding(
              padding: const EdgeInsets.all(16.0),
              child: FilterMenu(
                fetchFilterOptions: MovieService.fetchFilterOptions,
                onApplyFilter: (category, sort, genre, country, year) {
                  setState(() {
                    _selectedCategory = category;
                    _selectedSort = sort;
                    _selectedGenre = genre;
                    _selectedCountry = country;
                    _selectedYear = year;
                  });

                  Navigator.pop(context); // Đóng FilterMenu
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => FilterMoviesScreen(
                        category: _selectedCategory,
                        genreIds:
                            _selectedGenre != null ? [_selectedGenre!] : null,
                        countryIds: _selectedCountry != null
                            ? [_selectedCountry!]
                            : null,
                        actorIds: null,
                      ),
                    ),
                  );
                },
                currentCategory: _selectedCategory,
                currentSort: _selectedSort,
                currentGenre: _selectedGenre,
                currentCountry: _selectedCountry,
                currentYear: _selectedYear,
              ),
            ),
          );
        },
      ),
    );
  }

  Widget buildMovieSlider(List<Map<String, dynamic>> movies) {
    final PageController _pageController = PageController(
      viewportFraction: 1,
      initialPage: 1, // Bắt đầu từ trang ảo đầu tiên
    );

    return StatefulBuilder(
      builder: (context, setState) {
        int currentIndex = 1; // Bắt đầu từ trang ảo đầu tiên

        // Tự động chuyển slide
        Timer.periodic(Duration(seconds: 5), (Timer timer) {
          if (_pageController.hasClients) {
            if (currentIndex == movies.length + 1) {
              // Nếu ở slide ảo cuối, mượt mà quay về slide thực đầu tiên
              _pageController.animateToPage(
                1,
                duration: Duration(milliseconds: 1200),
                curve: Curves.easeInOut,
              );
              currentIndex = 1;
            } else if (currentIndex == 0) {
              // Nếu ở slide ảo đầu, mượt mà quay về slide thực cuối cùng
              _pageController.animateToPage(
                movies.length,
                duration: Duration(milliseconds: 600),
                curve: Curves.easeInOut,
              );
              currentIndex = movies.length;
            } else {
              currentIndex++;
              _pageController.animateToPage(
                currentIndex,
                duration: Duration(milliseconds: 600),
                curve: Curves.easeInOut,
              );
            }
          }
        });

        return SizedBox(
          height: 400,
          child: PageView.builder(
            controller: _pageController,
            itemCount: movies.length + 2, // Thêm 2 trang ảo
            onPageChanged: (index) {
              if (index == 0) {
                // Khi đến slide ảo đầu, mượt mà quay về slide thực cuối cùng
                Future.delayed(Duration(milliseconds: 600), () {
                  _pageController.jumpToPage(movies.length);
                });
              } else if (index == movies.length + 1) {
                // Khi đến slide ảo cuối, mượt mà quay về slide thực đầu tiên
                Future.delayed(Duration(milliseconds: 600), () {
                  _pageController.jumpToPage(1);
                });
              } else {
                currentIndex = index;
              }
            },
            itemBuilder: (context, index) {
              final movie = index == 0
                  ? movies[movies.length - 1] // Trang ảo đầu hiển thị phim cuối
                  : index == movies.length + 1
                      ? movies[0] // Trang ảo cuối hiển thị phim đầu
                      : movies[index - 1];

              final backgroundUrl = movie['backgroundUrl'] ?? '';
              final posterUrl = movie['posterUrl'] ?? '';
              final title = movie['title'] ?? 'Không có tiêu đề';
              final name = movie['name'] ?? 'Không có tiêu đề';
              return GestureDetector(
                onTap: () {
                  // Chuyển sang trang chi tiết phim
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) =>
                          MovieDetailsScreen(movieId: movie['movieId']),
                    ),
                  );
                },
                child: Stack(
                  children: [
                    // Nền
                    Container(
                      height: 400,
                      decoration: BoxDecoration(
                        image: DecorationImage(
                          image: NetworkImage(backgroundUrl),
                          fit: BoxFit.cover,
                        ),
                      ),
                      child: ShaderMask(
                        shaderCallback: (bounds) => LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [
                            Colors.transparent, // Bắt đầu trong suốt
                            Colors.black, // Kết thúc với màu đen
                          ],
                        ).createShader(bounds),
                        blendMode: BlendMode.srcOver,
                        child: Container(
                          color: Colors.black
                              .withOpacity(0.6), // Overlay màu đen nhẹ
                        ),
                      ),
                    ),

                    Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          ClipRRect(
                            borderRadius: BorderRadius.circular(10),
                            child: Image.network(
                              posterUrl,
                              height: 300,
                              width: 200,
                              fit: BoxFit.cover,
                              errorBuilder: (context, error, stackTrace) {
                                return Icon(Icons.broken_image,
                                    color: Colors.white, size: 80);
                              },
                            ),
                          ),
                          SizedBox(height: 16),
                          Text(
                            movie['title'],
                            style: TextStyle(
                                color: Colors.white,
                                fontSize: 18,
                                fontWeight: FontWeight.bold),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            textAlign: TextAlign.center,
                          ),
                          SizedBox(height: 2),
                          Text(
                            movie['name'] ?? '',
                            style: TextStyle(color: Colors.grey, fontSize: 14),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        );
      },
    );
  }

  Widget buildMovieCategory(String title, List<dynamic> movies, {String? category, List<int>? genreIds, List<int>? countryIds}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title,
                style: TextStyle(
                    fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
              ),
              TextButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => FilterMoviesScreen(
                        category: category,
                        genreIds: genreIds,
                        countryIds: countryIds,
                      ),
                    ),
                  );
                },
                child: Text(
                  'Xem Thêm',
                  style: TextStyle(color: Colors.pink.shade300),
                ),
              ),
            ],
          ),
        ),
        SizedBox(
          height: 260,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: movies.length,
            itemBuilder: (context, index) {
              final movie = movies[index];
              final isSeries = movie['isSeries'] ?? false;
              final totalEpisodes = movie['totalEpisodes'] ?? "0";
              final episodes = movie['episodes'] ?? [];
              final latestEpisode = episodes.isNotEmpty ? episodes.last['episodeNumber'] : "0";
              final status = movie['status'] ?? 'Đang cập nhật';

              return GestureDetector(
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => MovieDetailsScreen(movieId: movie['movieId']),
                    ),
                  );
                },
                child: Container(
                  width: 140,
                  margin: EdgeInsets.symmetric(horizontal: 8),
                  child: Stack(
                    children: [
                      // Poster với bo góc
                      ClipRRect(
                        borderRadius: BorderRadius.circular(10),
                        child: Image.network(
                          movie['posterUrl'] ?? '',
                          height: 200,
                          width: 140,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) {
                            return Icon(Icons.broken_image, color: Colors.white, size: 80);
                          },
                        ),
                      ),
                      // Trạng thái ở góc phải
                      // Trạng thái ở góc phải
                      Positioned(
                        top: 6,
                        right: 3,
                        child: Container(
                          padding: EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                          decoration: BoxDecoration(
                            color: Colors.pinkAccent.withOpacity(0.5),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            isSeries
                                ? (episodes.isNotEmpty ? "Tập $latestEpisode/$totalEpisodes" : status)
                                : status,
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ),

                      // Tên phim và thông tin
                      Positioned(
                        bottom: 0,
                        child: Container(
                          width: 140,
                          padding: EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: Colors.black.withOpacity(0.8),
                            borderRadius: BorderRadius.vertical(bottom: Radius.circular(10)),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(
                                movie['title'],
                                style: TextStyle(
                                    color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                textAlign: TextAlign.center,
                              ),
                              SizedBox(height: 2),
                              Text(
                                movie['name'] ?? '',
                                style: TextStyle(color: Colors.grey, fontSize: 10),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        leading: IconButton(
          icon: Transform.rotate(
            angle: 3.14, // Xoay 180 độ để đảo ngược biểu tượng
            child: Icon(Icons.logout, color: Colors.white),
          ),
          onPressed: () {
            // Đăng xuất và quay lại trang đăng nhập
            Navigator.pushReplacement(
              context,
              MaterialPageRoute(
                builder: (context) => LoginScreen(), // Thay LoginScreen bằng tên màn hình đăng nhập của bạn
              ),
            );
          },
        ),
        title: Image.asset(
          'assets/img.png',
          height: 40,
          fit: BoxFit.contain,
        ),
        centerTitle: true,
        actions: [
          IconButton(
            icon: Icon(Icons.filter_alt, color: Colors.white),
            onPressed: _showFilterMenu,
          ),
          IconButton(
            icon: Icon(Icons.search, color: Colors.white),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => SearchScreen(),
                ),
              );
            },
          ),
        ],
      ),


      body: RefreshIndicator(
        onRefresh: _refreshMovies,
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              FutureBuilder<List<Map<String, dynamic>>>(
                future: topRatedMoviesFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Center(child: CircularProgressIndicator());
                  } else if (snapshot.hasError) {
                    return Center(
                        child: Text('Lỗi: ${snapshot.error}',
                            style: TextStyle(color: Colors.white)));
                  } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                    return Center(
                        child: Text('Không có phim nào.',
                            style: TextStyle(color: Colors.white)));
                  }

                  final movies = snapshot.data!;
                  return buildMovieSlider(movies);
                },
              ),
              FutureBuilder<Map<String, List<dynamic>>>(
                future: categorizedMoviesFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Center(child: CircularProgressIndicator());
                  } else if (snapshot.hasError) {
                    return Center(
                        child: Text('Lỗi: ${snapshot.error}',
                            style: TextStyle(color: Colors.white)));
                  } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                    return Center(
                        child: Text('Không có phim nào.',
                            style: TextStyle(color: Colors.white)));
                  }

                  final data = snapshot.data!;
                  final moviesTheater = data['moviesTheater'] ?? [];
                  final moviesSeries = data['moviesSeries'] ?? [];
                  final moviesAnime = data['moviesAnime'] ?? [];
                  final moviesDramaKorean = data['moviesDramaKorean'] ?? [];
                  final moviesChineseHistoricalDrama = data['moviesChineseHistoricalDrama'] ?? [];
                  final moviesHorror = data['moviesHorror'] ?? [];

                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      buildMovieCategory('Phim Lẻ', moviesTheater, category: 'phimle'),
                      buildMovieCategory('Phim Bộ', moviesSeries, category: 'phimbo'),
                      buildMovieCategory('Phim Hoạt Hình', moviesAnime, genreIds: [2]),
                      buildMovieCategory('Phim Kinh Dị', moviesHorror, genreIds: [3]),
                      buildMovieCategory('Chính Kịch Hàn Quốc', moviesDramaKorean, genreIds: [20], countryIds: [6]),
                      // buildMovieCategory('Cổ Trang Trung Quốc', moviesChineseHistoricalDrama, genreIds: [14], countryIds: [8]),
                    ],
                  );
                },
              ),


            ],
          ),
        ),
      ),
    );
  }
}
