'use strict';

/**
 * Book controller.
 */
App.controller('Library', function ($scope, $timeout, Restangular, $stateParams) {
	/**
	 * View scope variables.
	 */
	$scope.sortColumn = 3;
	$scope.asc = true;
	$scope.offset = 0;
	$scope.limit = 20;
	$scope.search = {
		text: '',
		// read: false
	};
	$scope.loading = false;
	$scope.books = [];
	$scope.total = -1;
	$scope.rank = "";

	$scope.minRating = 0


	// get the author names from api/library/authors
	Restangular.one('library/authors').get().then(function (data) {
		$scope.authors = data.authors.map(function (authorName) {
			return {
				name: authorName,
				selected: false // initially, none of the authors are selected
			};
		});
	});


	$scope.availableGenres = [
		{ name: 'Romance', selected: false },
		{ name: 'Mystery', selected: false },
		{ name: 'Fantasy and Science Fiction', selected: false },
		{ name: 'Thriller and Horror', selected: false },
		{ name: 'Children', selected: false },
		{ name: 'Young Adult', selected: false },
		{ name: 'Inspiration, Self-Help and Religious', selected: false },
		{ name: 'Biography, Autobiography and Memoir', selected: false },
		{ name: 'Other', selected: false }
	];




	// A timeout promise is used to slow down search requests to the server
	// We keep track of it for cancellation purpose
	var timeoutPromise;

	/**
	 * Reload books.
	 */
	$scope.loadBooks = function () {
		$scope.offset = 0;
		$scope.total = -1;
		$scope.books = [];
		$scope.pageBooks();
	};

	$scope.$watch('rank', function () {
		if ($scope.rank === "") {
			// set genre and author filters to false and minRating to 0

			Restangular.one('library/list').get({
				offset: $scope.offset,
				limit: $scope.limit,
				sort_column: $scope.sortColumn,
				asc: $scope.asc,
				search: $scope.search.text,
				min_rating: $scope.minRating,
				authors: _.pluck(_.where($scope.authors, { selected: true }), 'name'),
				genres: _.pluck(_.where($scope.availableGenres, { selected: true }), 'name')
			}).then(function (data) {
				$scope.books = data.books;
				$scope.total = data.total;
				$scope.loading = false;
			});
		} else if ($scope.rank === "avgRating") {
			Restangular.one('library/top/avgRating/desc').get({
			}).then(function (data) {
				$scope.books = data.books;
				$scope.total = data.total;
				$scope.loading = false;
			});
		} else if ($scope.rank === "numRatings") {
			Restangular.one('library/top/numRatings/desc').get({
			}).then(function (data) {
				$scope.books = data.books;
				$scope.total = data.total;
				$scope.loading = false;
			});
		}
		$scope.pageBooks();
	});

	$scope.test = function () {
		console.log("test");
	};

	$scope.applyFilters = function () {
		Restangular.one('library/list').get({
			offset: $scope.offset,
			limit: $scope.limit,
			sort_column: $scope.sortColumn,
			asc: $scope.asc,
			search: $scope.search.text,
			min_rating: $scope.minRating,
			authors: _.pluck(_.where($scope.authors, { selected: true }), 'name'),
			genres: _.pluck(_.where($scope.availableGenres, { selected: true }), 'name')
		}).then(function (data) {
			$scope.books = data.books;
			$scope.total = data.total;
			$scope.loading = false;
		});
	};

	/**
	 * Load books.
	 */
	$scope.pageBooks = function (next) {
		if ($scope.loading || $scope.total == $scope.books.length) {
			// Avoid spamming the server
			return;
		}

		if (next) {
			$scope.offset += $scope.limit;
		}

		$scope.loading = true;
		Restangular.one('library/list').get({
			offset: $scope.offset,
			limit: $scope.limit,
			sort_column: $scope.sortColumn,
			asc: $scope.asc,
			search: $scope.search.text,
			// min_rating: $scope.minRating,
			// authors: _.pluck(_.where($scope.authors, { selected: true }), 'name'),
			// genres: _.pluck(_.where($scope.availableGenres, { selected: true }), 'name')
		}).then(function (data) {
			$scope.books = $scope.books.concat(data.books);
			$scope.total = data.total;
			$scope.loading = false;
		});
	};

	$scope.filterUsingRating = function () {
		Restangular.one('library/rating').get({
			offset: $scope.offset,
			limit: $scope.limit,
			genres: _.pluck(_.where($scope.availableGenres, { selected: true }), 'name'),
			sort_column: $scope.sortColumn,
			asc: $scope.asc,
			min_rating: $scope.minRating,
		}).then(function (data) {
			$scope.books = data.books;
			$scope.total = data.total;
			$scope.loading = false;
		}).catch(function(error) {
			alert("Rating not allowed, must be 0-9");
		});
	};

	$scope.filterUsingGenre = function () {
		Restangular.one('library/genres').get({
			offset: $scope.offset,
			limit: $scope.limit,
			authors: _.pluck(_.where($scope.authors, { selected: true }), 'name'),
			sort_column: $scope.sortColumn,
			asc: $scope.asc,
			genres: _.pluck(_.where($scope.availableGenres, { selected: true }), 'name'),
		}).then(function (data) {
			$scope.books = data.books;
			$scope.total = data.total;
			$scope.loading = false;
		});
	};

	$scope.filterUsingAuthor = function () {
		Restangular.one('library/filter_authors').get({
			offset: $scope.offset,
			limit: $scope.limit,
			genres: _.pluck(_.where($scope.availableGenres, { selected: true }), 'name'),
			sort_column: $scope.sortColumn,
			asc: $scope.asc,
			authors: _.pluck(_.where($scope.authors, { selected: true }), 'name'),
		}).then(function (data) {
			$scope.books = data.books;
			$scope.total = data.total;
			$scope.loading = false;
		});
	};



	/**
	 * Watch for search scope change.
	 */
	$scope.$watch('search', function () {
		if (timeoutPromise) {
			// Cancel previous timeout
			$timeout.cancel(timeoutPromise);
		}

		// Call API later
		timeoutPromise = $timeout(function () {
			$scope.loadBooks();
		}, 200);
	}, true);

	/**
	 * Go to book
	 */
	$scope.goToBook = function () {
		Restangular.one('/library/book/', { id: $scope.id }).get({ limit: 100 }).then(function () {
			$state.transitionTo('librarybook')
		});
	};
});