import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';
import '../services/movie_service.dart';
import 'filter_movies_screen.dart';
import 'package:html/parser.dart' as html;
import 'package:flutter/services.dart';
import 'movie_search.dart';
import 'filter_menu.dart';
import 'dart:ui';

String parseHtmlString(String htmlString) {
  final document = html.parse(htmlString);
  return document.body?.text ?? htmlString;
}
class MovieDetailsScreen extends StatefulWidget {
  final int movieId;

  MovieDetailsScreen({required this.movieId});

  @override
  _MovieDetailsScreenState createState() => _MovieDetailsScreenState();
}

class _MovieDetailsScreenState extends State<MovieDetailsScreen> {
  late Future<Map<String, dynamic>> movieDetailsFuture;
  bool showDetails = false;
  bool isCategoryPressed = false;
  bool isCountryPressed = false;
  String? _selectedCategory;
  String? _selectedSort;
  int? _selectedGenre;
  int? _selectedCountry;
  int? _selectedYear;
  @override
  void initState() {
    super.initState();
    movieDetailsFuture = MovieService.fetchMovieDetails(widget.movieId);
  }

  Widget buildEpisodeList(List<dynamic> episodes, bool isSeries) {
    if (!isSeries) {
      // Nếu không phải phim bộ, hiển thị "Tập Full"
      return Card(
        color: Colors.grey[900],
        margin: EdgeInsets.all(8.0),
        child: ListTile(
          title: Text(
            'Tập Full',
            style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
          ),
          trailing: Icon(Icons.play_arrow, color: Colors.white),
          onTap: () {
            // Điều hướng tới trình phát video với tập duy nhất
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => FullScreenVideoPlayer(
                  videoUrl: episodes.isNotEmpty ? episodes[0]['videoUrl'] ?? '' : '',
                  episodes: episodes,
                  currentEpisodeIndex: 0,
                ),
              ),
            );
          },
        ),
      );
    }

    // Nếu là phim bộ, tiếp tục chia nhóm tập
    const int episodesPerGroup = 50; // Số tập mỗi nhóm
    int currentGroupIndex = 0; // Chỉ số của nhóm tập hiện tại

    // Chia danh sách tập thành các nhóm
    List<List<dynamic>> groupedEpisodes = [];
    for (int i = 0; i < episodes.length; i += episodesPerGroup) {
      groupedEpisodes.add(episodes.sublist(
        i,
        i + episodesPerGroup > episodes.length ? episodes.length : i + episodesPerGroup,
      ));
    }

    return StatefulBuilder(
      builder: (BuildContext context, StateSetter setState) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Thanh điều hướng nhóm tập
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                // Nút Previous
                IconButton(
                  icon: Icon(
                    Icons.arrow_back,
                    color: currentGroupIndex > 0 ? Colors.pinkAccent : Colors.grey,
                  ),
                  onPressed: () {
                    if (currentGroupIndex > 0) {
                      setState(() {
                        currentGroupIndex--;
                      });
                    }
                  },
                ),

                // Hiển thị nhóm tập hiện tại
                Text(
                  'Tập ${(currentGroupIndex * episodesPerGroup) + 1} - ${((currentGroupIndex + 1) * episodesPerGroup > episodes.length ? episodes.length : (currentGroupIndex + 1) * episodesPerGroup)}',
                  style: TextStyle(fontSize: 16, color: Colors.white, fontWeight: FontWeight.bold),
                ),

                // Nút Next
                IconButton(
                  icon: Icon(
                    Icons.arrow_forward,
                    color: currentGroupIndex < groupedEpisodes.length - 1 ? Colors.pinkAccent : Colors.grey,
                  ),
                  onPressed: () {
                    if (currentGroupIndex < groupedEpisodes.length - 1) {
                      setState(() {
                        currentGroupIndex++;
                      });
                    }
                  },
                ),
              ],
            ),

            // Danh sách tập trong nhóm hiện tại (2 tập mỗi hàng)
            GridView.builder(
              shrinkWrap: true,
              physics: NeverScrollableScrollPhysics(),
              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2, // Số tập mỗi hàng
                childAspectRatio: 3, // Tỷ lệ chiều rộng/chiều cao
              ),
              itemCount: groupedEpisodes[currentGroupIndex].length,
              itemBuilder: (context, index) {
                final episode = groupedEpisodes[currentGroupIndex][index];
                final episodeNumber = (currentGroupIndex * episodesPerGroup) + index + 1;

                return Card(
                  color: Colors.grey[900],
                  margin: EdgeInsets.all(8.0),
                  child: ListTile(
                    title: Text(
                      'Tập $episodeNumber',
                      style: TextStyle(color: Colors.white),
                    ),
                    trailing: Icon(Icons.play_arrow, color: Colors.white),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => FullScreenVideoPlayer(
                            videoUrl: episode['videoUrl'] ?? '',
                            episodes: episodes,
                            currentEpisodeIndex: episodeNumber - 1,
                          ),
                        ),
                      );
                    },
                  ),
                );
              },
            ),
          ],
        );
      },
    );
  }



  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: Image.asset(
          'assets/img.png', // Đường dẫn tới ảnh logo
          height: 40,       // Chiều cao logo
          fit: BoxFit.contain,
        ),
        centerTitle: true,
        actions: [

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
        ],// Căn giữa logo
      ),
      body: FutureBuilder<Map<String, dynamic>>(
        future: movieDetailsFuture,
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
          } else if (!snapshot.hasData) {
            return Center(
              child: Text(
                'Không tìm thấy dữ liệu.',
                style: TextStyle(color: Colors.white),
              ),
            );
          }

          final data = snapshot.data!;
          final trailerUrl = data['trailerUrl'] ?? '';
          final episodes = data['episodes'] ?? [];
          final backgroundUrl = data['backgroundUrl'] ?? '';
          final posterUrl = data['posterUrl'] ?? '';
          final title = data['title'] ?? '';
          final name = data['name'] ?? '';
          final descriptionHtml = data['description'] ?? 'Không rõ';
          final String director = data['director'] ?? 'Không rõ';
          final int releaseYear = data['releaseYear'] ?? 'Không rõ';
          final moviesInSameGenre = List<Map<String, dynamic>>.from(data['moviesInSameGenre'] ?? []);
          // Kiểm tra và xử lý dữ liệu của country
          final Map<String, dynamic> countryData = (data['country'] as Map<String, dynamic>? ?? {
            'countryId': 0,
            'countryName': 'Không rõ',
          });
          final int countryId = countryData['countryId'] ?? 0; // Lấy ID quốc gia
          final String countryName = countryData['countryName'] ?? 'Không rõ'; // Lấy tên quốc gia


          final String duration = data['duration']?.toString() ?? 'Không rõ';
          final String status = data['status'] ?? 'Không rõ';
          final String totalEpisodes = data['totalEpisodes']?.toString() ?? 'Không rõ';
          final bool isSeries = data['isSeries'] ?? false;
          final List<Map<String, dynamic>> genres = List<Map<String, dynamic>>.from(
              data['genres'] ?? []);
          final List<Map<String, dynamic>> actors = List<Map<String, dynamic>>.from(
              data['actors'] ?? []);


          final int latestEpisode = episodes.isNotEmpty
              ? episodes.map((e) => int.tryParse(e['episodeNumber']?.toString() ?? '0') ?? 0).reduce((a, b) => a > b ? a : b)
              : 0;

          final String episodeInfo = isSeries
              ? "$latestEpisode/$totalEpisodes"
              : "Không áp dụng";

          return SingleChildScrollView(
            child: Container(
              color: Colors.black, // Đặt nền màu đen
              constraints: BoxConstraints(
                minHeight: MediaQuery.of(context).size.height, // Đảm bảo chiều cao tối thiểu là chiều cao màn hình
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Phần tiêu đề với poster và backgroundUrl
                  Stack(
                    children: [
                      Container(
                        height: 300,
                        decoration: BoxDecoration(
                          color: Colors.black,
                          image: backgroundUrl.isNotEmpty
                              ? DecorationImage(
                            image: NetworkImage(backgroundUrl),
                            fit: BoxFit.cover,
                          )
                              : null,
                        ),
                        child: Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.bottomCenter,
                              end: Alignment.topCenter,
                              colors: [
                                Colors.black.withOpacity(0.8),
                                Colors.transparent,
                              ],
                              stops: [0.0, 1.0],
                            ),
                          ),
                        ),
                      ),
                      Positioned(
                        left: 16,
                        bottom: 16,
                        child: Container(
                          width: 100,
                          height: 150,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(8),
                            image: posterUrl.isNotEmpty
                                ? DecorationImage(
                              image: NetworkImage(posterUrl),
                              fit: BoxFit.cover,
                            )
                                : null,
                            color: Colors.black,
                          ),
                        ),
                      ),
                      Positioned(
                        left: 130,
                        bottom: 14,
                        right: 16,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              title,
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: Colors.white,
                                shadows: [
                                  Shadow(
                                    blurRadius: 10.0,
                                    color: Colors.black,
                                    offset: Offset(2.0, 2.0),
                                  ),
                                ],
                              ),
                            ),
                            SizedBox(height: 8), // Khoảng cách giữa title và name
                            Text(
                              name ?? '',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w400,
                                color: Colors.white70,
                                shadows: [
                                  Shadow(
                                    blurRadius: 10.0,
                                    color: Colors.black,
                                    offset: Offset(2.0, 2.0),
                                  ),
                                ],
                              ),
                            ),
                            SizedBox(height: 8),
                            Text(
                              releaseYear?.toString() ?? '',
                              style: TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w400,
                                color: Colors.white30,
                                shadows: [
                                  Shadow(
                                    blurRadius: 10.0,
                                    color: Colors.black,
                                    offset: Offset(2.0, 2.0),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Text(
                      parseHtmlString(descriptionHtml).length > 200 && !showDetails
                          ? parseHtmlString(descriptionHtml).substring(0, 200) + "..."
                          : parseHtmlString(descriptionHtml),
                      style: TextStyle(fontSize: 16, color: Colors.white),
                    ),
                  ),
                  Center(
                    child: Column(
                      children: [
                        TextButton(
                          style: TextButton.styleFrom(
                            backgroundColor: Colors.pinkAccent.withOpacity(0.2),
                            padding: EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10),
                            ),
                          ),
                          onPressed: () {
                            setState(() {
                              showDetails = !showDetails;
                            });
                          },
                          child: Text(
                            showDetails ? "Ẩn bớt" : "Xem thêm",
                            style: TextStyle(
                              color: Colors.pinkAccent,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        Icon(
                          showDetails ? Icons.expand_less : Icons.expand_more,
                          color: Colors.pinkAccent,
                          size: 24,
                        ),
                      ],
                    ),
                  ),
              if (showDetails)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Danh mục và Quốc gia
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      flex: 1,
                      child: Row(
                        children: [
                          Text(
                            "Danh mục: ",
                            style: TextStyle(fontSize: 16, color: Colors.grey),
                          ),
                          GestureDetector(
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => FilterMoviesScreen(
                                    category: isSeries ? "phimbo" : "phimle",
                                  ),
                                ),
                              );
                            },
                            child: Text(
                              isSeries ? "Phim Bộ" : "Phim Lẻ",
                              style: TextStyle(
                                color: Colors.pinkAccent,
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    Expanded(
                      flex: 1,
                      child: Row(
                        children: [
                          Text(
                            "Quốc gia: ",
                            style: TextStyle(fontSize: 16, color: Colors.grey),
                          ),
                          GestureDetector(
                            onTap: () {
                              final countryId = data['country']['countryId'];
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => FilterMoviesScreen(
                                    countryIds: [countryId],
                                  ),
                                ),
                              );
                            },
                            child: Text(
                              data['country']['countryName'],
                              style: TextStyle(
                                color: Colors.pinkAccent,
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                SizedBox(height: 4), // Khoảng cách giữa các hàng

                // Thời lượng và Trạng thái
                Row(
                  children: [
                    Expanded(
                      child: _buildDetailItem(
                        "Thời lượng:",
                        isSeries ? "$duration phút/tập" : "$duration phút",
                      ),
                    ),
                    Expanded(
                      child: _buildDetailItem(
                        "Trạng thái:",
                        isSeries ? "Tập $episodeInfo" : status,
                      ),
                    ),
                  ],
                ),
                SizedBox(height: 4),

                // Đạo diễn
                _buildDetailItem(
                  "Đạo diễn:",
                  "$director",
                ),
                SizedBox(height: 4),

                // Thể loại
                // Thể loại
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "Thể loại:",
                      style: TextStyle(fontSize: 16, color: Colors.grey),
                    ),
                    SizedBox(width: 8), // Khoảng cách giữa tiêu đề và danh sách
                    Expanded(
                      child: Wrap(
                        children: genres.isEmpty
                            ? [
                          Text(
                            "Đang cập nhật",
                            style: TextStyle(
                              fontSize: 16,
                              color: Colors.white70,
                            ),
                          ),
                        ]
                            : [
                          for (int i = 0; i < genres.length; i++) ...[
                            GestureDetector(
                              onTap: () {
                                // Khi nhấn vào thể loại, chuyển đến trang FilterMoviesScreen với genreId
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => FilterMoviesScreen(
                                      genreIds: [genres[i]['genreId'] as int],
                                    ),
                                  ),
                                );
                              },
                              child: Text(
                                genres[i]['genreName'],
                                style: TextStyle(
                                  color: Colors.pinkAccent,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            if (i < genres.length - 1) // Thêm dấu phẩy trừ mục cuối
                              Text(
                                ", ",
                                style: TextStyle(color: Colors.white),
                              ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),

                SizedBox(height: 4),

                // Diễn viên
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "Diễn viên:",
                      style: TextStyle(fontSize: 16, color: Colors.grey),
                    ),
                    SizedBox(width: 8), // Khoảng cách giữa tiêu đề và danh sách
                    Expanded(
                      child: Wrap(
                        children: actors.isEmpty
                            ? [
                          Text(
                            "Đang cập nhật",
                            style: TextStyle(
                              fontSize: 16,
                              color: Colors.white70,
                            ),
                          ),
                        ]
                            : [
                          for (int i = 0; i < actors.length; i++) ...[
                            GestureDetector(
                              onTap: () {
                                // Khi nhấn vào diễn viên, chuyển đến trang FilterMoviesScreen với actorId
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => FilterMoviesScreen(
                                      actorIds: [actors[i]['actorId'] as int],
                                    ),
                                  ),
                                );
                              },
                              child: Text(
                                actors[i]['actorName'],
                                style: TextStyle(
                                  color: Colors.pinkAccent,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            if (i < actors.length - 1) // Thêm dấu phẩy trừ mục cuối
                              Text(
                                ", ",
                                style: TextStyle(color: Colors.white),
                              ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),

              ],
            ),
          ),

          SizedBox(height: 4),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          episodes.isNotEmpty ? "Danh Sách Tập" : "Sắp chiếu",
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                        ),
                        if (trailerUrl.isNotEmpty)
                          TextButton.icon(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) =>
                                      FullScreenTrailerPlayer(trailerUrl: trailerUrl),
                                ),
                              );
                            },
                            icon: Icon(Icons.play_circle_filled, color: Colors.red),
                            label: Text(
                              "Xem Trailer",
                              style: TextStyle(color: Colors.red),
                            ),
                          ),
                      ],
                    ),
                  ),
                  if (episodes.isNotEmpty)
                    buildEpisodeList(episodes, isSeries),
                  if (moviesInSameGenre.isNotEmpty)
                    buildMovieCategory(
                      "Phim Cùng Thể Loại",
                      moviesInSameGenre,
                      genreIds: [/* ID thể loại hiện tại */],
                    ),

                ],
              ),
            ),
          );
        },
      ),
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

            ],
          ),
        ),
        SizedBox(
          height: 260, // Giữ nguyên chiều cao
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
                  child: Column(
                    children: [
                      // Poster với bo góc
                      ClipRRect(
                        borderRadius: BorderRadius.circular(10),
                        child: Image.network(
                          movie['posterUrl'] ?? '',
                          height: 200, // Giữ chiều cao poster cố định
                          width: 140,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) {
                            return Icon(Icons.broken_image, color: Colors.white, size: 80);
                          },
                        ),
                      ),
                      SizedBox(height: 4), // Khoảng cách giữa poster và text
                      // Phần thông tin phim
                      Expanded(
                        child: Container(
                          padding: EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: Colors.black.withOpacity(0.8),
                            borderRadius: BorderRadius.vertical(bottom: Radius.circular(10)),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Flexible(
                                child: Text(
                                  movie['title'] ?? 'Không rõ',
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 12,
                                    fontWeight: FontWeight.bold,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  textAlign: TextAlign.start,
                                ),
                              ),
                              SizedBox(height: 2),
                              Flexible(
                                child: Text(
                                  movie['name'] ?? '',
                                  style: TextStyle(color: Colors.grey, fontSize: 10),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  textAlign: TextAlign.start,
                                ),
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

  Widget _buildDetailItem(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: TextStyle(fontSize: 16, color: Colors.grey),
          ),
          SizedBox(width: 8),
          Expanded(
            child: Text(
              value,
              style: TextStyle(fontSize: 16, color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }

}

class FullScreenTrailerPlayer extends StatefulWidget {
  final String trailerUrl;

  FullScreenTrailerPlayer({required this.trailerUrl});

  @override
  _FullScreenTrailerPlayerState createState() => _FullScreenTrailerPlayerState();
}

class _FullScreenTrailerPlayerState extends State<FullScreenTrailerPlayer> {
  final WebViewController webViewController = WebViewController();

  @override
  void initState() {
    super.initState();
    // Đặt màn hình xoay ngang ngay khi mở trailer
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    SystemChrome.setPreferredOrientations([DeviceOrientation.landscapeLeft, DeviceOrientation.landscapeRight]);
    webViewController
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..loadRequest(Uri.parse('${widget.trailerUrl}?autoplay=1'));
  }

  @override
  void dispose() {
    // Reset lại hướng màn hình về dọc khi thoát
    SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp, DeviceOrientation.portraitDown]);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          // Hiển thị trailer trong WebView
          WebViewWidget(controller: webViewController),
          // Nút thoát ở góc trên
          Positioned(
            top: 16,
            left: 16,
            child: IconButton(
              icon: Icon(Icons.arrow_back, color: Colors.white, size: 30),
              onPressed: () {
                Navigator.pop(context);
              },
            ),
          ),
        ],
      ),
    );
  }
}




class FullScreenVideoPlayer extends StatefulWidget {
  final String videoUrl;
  final List<dynamic> episodes;
  final int currentEpisodeIndex;

  FullScreenVideoPlayer({
    required this.videoUrl,
    required this.episodes,
    required this.currentEpisodeIndex,
  });

  @override
  _FullScreenVideoPlayerState createState() => _FullScreenVideoPlayerState();
}

class _FullScreenVideoPlayerState extends State<FullScreenVideoPlayer> {
  late WebViewController webViewController;
  late ScrollController _scrollController;
  bool _showMenu = false;
  late int currentEpisodeIndex;



  @override
  void initState() {
    super.initState();
    currentEpisodeIndex = widget.currentEpisodeIndex;

    // Khởi tạo WebViewController
    webViewController = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..loadRequest(Uri.parse('${widget.videoUrl}?autoplay=1'));

    // Khởi tạo ScrollController
    _scrollController = ScrollController();

    // Đặt chế độ toàn màn hình khi mở video
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);

    // Đặt xoay ngang khi mở video
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
  }

  void _resetOrientation() {
    // Reset lại xoay dọc và hiển thị UI khi thoát
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _resetOrientation();
    super.dispose();
  }

  void _toggleMenu() {
    setState(() {
      _showMenu = !_showMenu;

      if (_showMenu) {
        // Tự động cuộn tập hiện tại lên đầu danh sách
        WidgetsBinding.instance.addPostFrameCallback((_) {
          _scrollController.jumpTo(
            (currentEpisodeIndex * 50.0).clamp(0.0, _scrollController.position.maxScrollExtent),
          );
        });
      }
    });
  }

  void _playSelectedEpisode(int index) {
    setState(() {
      currentEpisodeIndex = index;
    });

    // Thay đổi URL trong WebView
    webViewController.loadRequest(Uri.parse('${widget.episodes[index]['videoUrl'] ?? ''}?autoplay=1'));
  }



  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          // Hiển thị video trong WebView
          WebViewWidget(controller: webViewController),
          // Nút menu ở góc trên bên trái
          Positioned(
            top: 16,
            left: 16,
            child: IconButton(
              icon: Icon(Icons.menu, color: Colors.white, size: 30),
              onPressed: _toggleMenu,
            ),
          ),
          // Menu hiển thị danh sách tập
          if (_showMenu)
            Positioned(
              top: 60,
              left: 16,
              child: Container(
                width: 115, // Tăng chiều rộng menu
                height: MediaQuery.of(context).size.height - 120,
                padding: EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.black.withOpacity(0.8),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    IconButton(
                      icon: Icon(Icons.arrow_back, color: Colors.white),
                      onPressed: () {
                        Navigator.pop(context);
                      },
                    ),
                    Expanded(
                      child: ListView.builder(
                        controller: _scrollController,
                        itemCount: widget.episodes.length,
                        itemBuilder: (context, index) {
                          final episode = widget.episodes[index];
                          final isCurrentEpisode = index == currentEpisodeIndex;

                          return ListTile(
                            title: Text(
                              'Tập ${index + 1}',
                              style: TextStyle(
                                color: isCurrentEpisode ? Colors.white : Colors.grey,
                                fontSize: 14,
                              ),
                            ),
                            tileColor: isCurrentEpisode ? Colors.pinkAccent : Colors.transparent, // Màu nền nổi bật
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(8), // Tạo góc bo tròn cho ô tập hiện tại
                            ),
                            onTap: () {
                              if (!isCurrentEpisode) {
                                _playSelectedEpisode(index);
                              }
                            },
                          );

                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}


