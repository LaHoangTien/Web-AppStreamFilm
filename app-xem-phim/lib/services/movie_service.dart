import 'dart:convert';
import 'package:http/http.dart' as http;

class MovieService {
  static const String baseUrl = 'http://localhost:8080/api'; // Base URL API của bạn

  // Lấy danh sách phim theo danh mục (phân loại)
  static Future<Map<String, List<dynamic>>> fetchCategorizedMovies() async {
    final response = await http.get(Uri.parse('$baseUrl/categorized-movies'));

    if (response.statusCode == 200) {
      final data = json.decode(response.body) as Map<String, dynamic>;
      return {
        "moviesTheater": data['moviesTheater'],
        "moviesSeries": data['moviesSeries'],
        "moviesAnime": data['moviesAnime'],
        "moviesDramaKorean": data['moviesDramaKorean'],
        "moviesChineseHistoricalDrama": data['moviesChineseHistoricalDrama'],
        "moviesHorror": data['moviesHorror'],
      };
    } else {
      throw Exception('Failed to load categorized movies');
    }
  }

  // Lấy danh sách phim theo bộ lọc
  static Future<Map<String, dynamic>> fetchFilteredMovies({
    String? keyword,
    String? sort,
    String? cat,
    List<int>? genreIds,
    List<int>? actorIds,
    List<int>? countryIds,
    int? releaseYear,
    int page = 0,
  }) async {
    final queryParameters = {
      if (keyword != null && keyword.isNotEmpty) 'keyword': keyword,
      if (sort != null) 'sort': sort,
      if (cat != null) 'cat': cat,
      if (genreIds != null && genreIds.isNotEmpty) 'genreIds': genreIds.join(','),
      if (actorIds != null && actorIds.isNotEmpty) 'actorIds': actorIds.join(','),
      if (countryIds != null && countryIds.isNotEmpty) 'countryIds': countryIds.join(','),
      if (releaseYear != null) 'releaseYear': releaseYear.toString(),
      'page': page.toString(),
    };

    final uri = Uri.parse('$baseUrl/filter-movies').replace(queryParameters: queryParameters);
    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return {
        'movies': data['movies'],
        'totalPages': data['totalPages'], // Lấy tổng số trang từ API
      };
    } else {
      throw Exception('Failed to load movies');
    }
  }


  // Lấy các tùy chọn lọc (thể loại, quốc gia, năm phát hành)
  static Future<Map<String, dynamic>> fetchFilterOptions() async {
    final response = await http.get(Uri.parse('$baseUrl/filter-options'));

    if (response.statusCode == 200) {
      return json.decode(response.body); // Trả về {genres, countries, releaseYears}
    } else {
      throw Exception('Failed to load filter options');
    }
  }
  static Future<Map<String, dynamic>> fetchMovieDetails(int movieId) async {
    final response = await http.get(Uri.parse('$baseUrl/movies/$movieId/details'));

    if (response.statusCode == 200) {
      try {
        // Parse JSON nếu phản hồi hợp lệ
        return json.decode(response.body) as Map<String, dynamic>;
      } catch (e) {
        // Xử lý lỗi nếu phản hồi không phải JSON hợp lệ
        throw Exception('Phản hồi không hợp lệ: ${response.body}');
      }
    } else {
      throw Exception('Failed to load movie details: ${response.body}');
    }
  }
  static Future<List<Map<String, dynamic>>> searchMovies(String keyword) async {
    final response = await http.get(Uri.parse('$baseUrl/live?keyword=$keyword'));

    if (response.statusCode == 200) {
      try {
        // Parse JSON response
        return List<Map<String, dynamic>>.from(json.decode(response.body));
      } catch (e) {
        throw Exception('Invalid response: ${response.body}');
      }
    } else {
      throw Exception('Failed to fetch search results: ${response.body}');
    }
  }
  static Future<List<Map<String, dynamic>>> getTopRatedMovies() async {
    final response = await http.get(Uri.parse('$baseUrl/top-rated'));

    if (response.statusCode == 200) {
      try {
        // Parse JSON và lấy danh sách phim từ key "topRatedMovies"
        final Map<String, dynamic> jsonResponse = json.decode(response.body);
        final List<dynamic> topRatedMovies = jsonResponse['topRatedMovies'] ?? [];
        return topRatedMovies.map((e) => Map<String, dynamic>.from(e)).toList();
      } catch (e) {
        throw Exception('Invalid response format: ${response.body}');
      }
    } else {
      throw Exception('Failed to fetch top-rated movies: ${response.body}');
    }
  }

}
