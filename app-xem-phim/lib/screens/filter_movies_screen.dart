import 'package:flutter/material.dart';
import '../services/movie_service.dart';
import 'filter_menu.dart'; // Import FilterMenu
import 'movie_detail.dart';
import 'movie_search.dart';
class FilterMoviesScreen extends StatefulWidget {
  final String? category;
  final List<int>? genreIds;
  final List<int>? actorIds;
  final List<int>? countryIds;
  final String? sort;
  final int? releaseYear;

  FilterMoviesScreen({
    this.category,
    this.genreIds,
    this.actorIds,
    this.countryIds,
    this.sort,
    this.releaseYear,
  });

  @override
  _FilterMoviesScreenState createState() => _FilterMoviesScreenState();
}

class _FilterMoviesScreenState extends State<FilterMoviesScreen> {
  late Future<List<dynamic>> moviesFuture;

  String? _currentCategory;
  String? _currentSort;
  int? _currentGenre;
  int? _currentCountry;
  int? _currentYear;
  int _currentPage = 0;
  int _totalPages = 1;

  @override
  void initState() {
    super.initState();
    _currentCategory = widget.category;
    _currentSort = widget.sort;
    _currentGenre = widget.genreIds?.first;
    _currentCountry = widget.countryIds?.first;
    _currentYear = widget.releaseYear;

    // Khởi tạo giá trị mặc định cho moviesFuture
    moviesFuture = Future.value([]); // Khởi tạo với danh sách trống
    _fetchMovies(); // Gọi hàm fetch để cập nhật moviesFuture
  }

  Future<void> _fetchMovies() async {
    final result = await MovieService.fetchFilteredMovies(
      cat: _currentCategory,
      genreIds: widget.genreIds,
      countryIds: widget.countryIds,
      actorIds: widget.actorIds,
      releaseYear: _currentYear,
      sort: _currentSort,
      page: _currentPage,
    );

    print('Movies fetched: ${result['movies'].length}');

    setState(() {
      moviesFuture = Future.value(result['movies']);
      _totalPages = result['totalPages'];
    });
  }





  Future<void> _showFilterMenu() async {
    Navigator.push(
      context,
      MaterialPageRoute(
        fullscreenDialog: true,
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
            backgroundColor: Colors.black,
            body: Padding(
              padding: const EdgeInsets.all(16.0),
              child: FilterMenu(
                fetchFilterOptions: MovieService.fetchFilterOptions,
                onApplyFilter: (category, sort, genre, country, year) {
                  setState(() {
                    _currentCategory = category;
                    _currentSort = sort;
                    _currentGenre = genre;
                    _currentCountry = country;
                    _currentYear = year; // Cập nhật năm phát hành
                  });

                  Navigator.pop(context); // Đóng FilterMenu

                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => FilterMoviesScreen(
                        category: _currentCategory,
                        genreIds: _currentGenre != null ? [_currentGenre!] : null,
                        countryIds: _currentCountry != null ? [_currentCountry!] : null,
                        actorIds: null,
                        releaseYear: _currentYear, // Truyền năm phát hành
                      ),
                    ),
                  );
                },
                currentCategory: _currentCategory,
                currentSort: _currentSort,
                currentGenre: _currentGenre,
                currentCountry: _currentCountry,
                currentYear: _currentYear, // Truyền năm hiện tại
              ),
            ),
          );
        },
      ),
    );
  }

  Widget buildMovieCard(dynamic movie) {
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
        margin: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Phần Stack cho poster và trạng thái
            Stack(
              children: [
                // Phần poster với bo góc
                ClipRRect(
                  borderRadius: BorderRadius.circular(10), // Bo góc chỉ áp dụng cho poster
                  child: Image.network(
                    movie['posterUrl'] ?? '',
                    height: 220, // Tăng chiều cao poster
                    width: 140,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) {
                      return Icon(Icons.broken_image, color: Colors.white, size: 80);
                    },
                  ),
                ),
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
              ],
            ),
            SizedBox(height: 8), // Khoảng cách giữa poster và thông tin
            // Phần thông tin tên phim và tên gốc
            Column(
              children: [
                Text(
                  movie['title'] ?? 'Không có tiêu đề',
                  style: TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
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
          ],
        ),
      ),
    );
  }




  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: Text('Lọc Phim', style: TextStyle(color: Colors.white)),
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
      backgroundColor: Colors.black,
      body: Column(
        children: [
          Expanded(
            child: FutureBuilder<List<dynamic>>(
              future: moviesFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return Center(child: CircularProgressIndicator());
                } else if (snapshot.hasError) {
                  return Center(child: Text('Lỗi: ${snapshot.error}', style: TextStyle(color: Colors.white)));
                } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                  return Center(child: Text('Không tìm thấy phim nào.', style: TextStyle(color: Colors.white)));
                }

                final movies = snapshot.data!;

                return GridView.builder(
                  padding: EdgeInsets.all(8),
                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    crossAxisSpacing: 8,
                    mainAxisSpacing: 8,
                    childAspectRatio: 0.7,
                  ),
                  itemCount: movies.length,
                  itemBuilder: (context, index) {
                    final movie = movies[index];
                    return buildMovieCard(movie);
                  },
                );
              },
            ),
          ),
          // Chỉ hiển thị phân trang khi có nhiều hơn 1 trang
          if (_totalPages > 1)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 12.0, horizontal: 16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Nút Previous
                  IconButton(
                    icon: Icon(Icons.arrow_back_ios, color: _currentPage > 0 ? Colors.white : Colors.grey),
                    onPressed: _currentPage > 0
                        ? () {
                      setState(() {
                        _currentPage--;
                      });
                      _fetchMovies();
                    }
                        : null,
                  ),

                  // Hiển thị số trang
                  Container(
                    padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.grey[800],
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      'Page ${_currentPage + 1} / $_totalPages',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                  ),

                  // Nút Next
                  IconButton(
                    icon: Icon(Icons.arrow_forward_ios, color: _currentPage < _totalPages - 1 ? Colors.white : Colors.grey),
                    onPressed: _currentPage < _totalPages - 1
                        ? () {
                      setState(() {
                        _currentPage++;
                      });
                      _fetchMovies();
                    }
                        : null,
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }


}

