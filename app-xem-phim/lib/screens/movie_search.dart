import 'package:flutter/material.dart';
import '../services/movie_service.dart';
import 'movie_detail.dart';

class SearchScreen extends StatefulWidget {
  @override
  _SearchScreenState createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  String keyword = '';
  Future<List<Map<String, dynamic>>>? searchResultsFuture;
  Future<List<Map<String, dynamic>>>? topRatedMoviesFuture;

  @override
  void initState() {
    super.initState();
    // Gọi API để lấy danh sách top 6 phim đánh giá cao khi mở trang
    topRatedMoviesFuture = MovieService.getTopRatedMovies();
  }

  void _searchMovies() {
    if (keyword.isNotEmpty) {
      setState(() {
        searchResultsFuture = MovieService.searchMovies(keyword);
      });
    }
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
          title: Row(
            children: [
              Expanded(
                child: TextField(
                  onChanged: (value) => keyword = value,
                  onSubmitted: (_) => _searchMovies(),
                  style: TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    hintText: 'Tìm kiếm phim...',
                    hintStyle: TextStyle(color: Colors.grey),
                    border: InputBorder.none,
                  ),
                ),
              ),
              IconButton(
                icon: Icon(Icons.search, color: Colors.white),
                onPressed: _searchMovies,
              ),
            ],
          ),
        ),
      body: Container(
        color: Colors.black,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Text(
                searchResultsFuture == null ? 'Phim đề xuất' : 'Kết quả hàng đầu',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            Expanded(
              child: searchResultsFuture == null
                  ? FutureBuilder<List<Map<String, dynamic>>>(
                future: topRatedMoviesFuture, // Lấy top phim khi không có từ khóa
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Center(child: CircularProgressIndicator());
                  } else if (snapshot.hasError) {
                    return Center(
                      child: Text(
                        'Lỗi: ${snapshot.error}',
                        style: TextStyle(color: Colors.white),
                      ),
                    );
                  } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                    return Center(
                      child: Text(
                        'Không có phim nào.',
                        style: TextStyle(color: Colors.white),
                      ),
                    );
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
              )
                  : FutureBuilder<List<Map<String, dynamic>>>(
                future: searchResultsFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Center(child: CircularProgressIndicator());
                  } else if (snapshot.hasError) {
                    return Center(
                      child: Text(
                        'Lỗi: ${snapshot.error}',
                        style: TextStyle(color: Colors.white),
                      ),
                    );
                  } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                    return Center(
                      child: Text(
                        'Không tìm thấy kết quả nào.',
                        style: TextStyle(color: Colors.white),
                      ),
                    );
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
          ],
        ),
      ),


    );
  }
}
