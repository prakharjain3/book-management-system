'use strict';

/**
 * Book view controller.
 */
App.controller('BookView', function($scope, $q, $timeout, $state, $stateParams, Restangular) {
  /**
   * Delete the book.
   */
  $scope.deleteBook = function() {
    if(confirm('Do you really want to delete this book?')) {
      Restangular.one('book', $stateParams.id).remove().then(function() {
        $state.transitionTo('book');
      });
    }
  };

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

  $scope.submitGenres = function() {
    // Filter for selected genres
    let selectedGenres = $scope.availableGenres.filter(function(genre) {
      return genre.selected;
    }).map(function(genre) {
      return genre.name;
    });

    // Ensure there's a book selected and genres have been chosen
    if (!$scope.book || selectedGenres.length === 0) {
      alert('No book selected or no genres chosen.');
      return;
    }

    let genresPayload = {
      genres: selectedGenres
    };

    let bookId = $scope.book.bookId;

    // Send a POST request to the /api/book/genres/{bookid} endpoint
    Restangular.one('book', 'genres').one(bookId).post('', genresPayload).then(function(response) {
      alert('Genres updated successfully');
    }).catch(function(error) {
      console.error('Error updating genres:', error);
      alert('Error updating genres');
    });
  };



  /**
   * Edit the book.
   */
  $scope.editBook = function() {
    $state.transitionTo('bookedit', { id: $stateParams.id });
  }

  /**
 * Add the book to the library.
 */
  $scope.addToLibrary = function() {
    // Get the book id from the $stateParams
    var bookId = $scope.book.bookId;
    
    // Send a POST request to library/add with the book id
    Restangular.all('library/add').post({ bookId: bookId }).then(function() {
      alert('Book added to the library successfully');
      // call the submitGenres function
      $scope.submitGenres();
    }).catch(function(error) {
      console.error('Error adding the book to the library:', error);
    });
    
  };

  /**
   * Edit the book cover.
   */
  $scope.editCover = function() {
    var url = prompt('Book cover image URL (only JPEG supported)');
    if (url) {
      $scope.coverChanging = true;

      Restangular.one('book', $stateParams.id).post('cover', {url: url}).then(function() {
        $scope.coverChanging = false;
      }, function() {
        alert('Error downloading the book cover, please check the URL');
        $scope.coverChanging = false;
      });
    }
  };

  // True if the cover is in the process of being changed
  $scope.coverChanging = false;

  // Load tags
  var tagsPromise = Restangular.one('tag/list').get().then(function(data) {
    $scope.tags = data.tags;
  });
  
  // Load book
  var bookPromise = Restangular.one('book', $stateParams.id).get().then(function(data) {
    $scope.book = data;
  })

  // Wait for everything to load
  $q.all([bookPromise, tagsPromise]).then(function() {
    $timeout(function () {
      // Initialize active tags
      _.each($scope.tags, function(tag) {
        var found = _.find($scope.book.tags, function(bookTag) {
          return tag.id == bookTag.id;
        });
        tag.active = found !== undefined;
      });

      // Initialize read state
      $scope.book.read = $scope.book.read_date != null;

      $scope.book.favourite = $scope.book.favourite == 1;

      // Watch tags activation
      $scope.$watch('tags', function(prev, next) {
        if (prev && next && !angular.equals(prev, next)) {
          Restangular.one('book', $stateParams.id).post('', {
            tags: _.pluck(_.where($scope.tags, { active: true }), 'id')
          })
        }
      }, true);

      // Watch read state change
      $scope.$watch('book.read', function(prev, next) {
        if (prev !== next) {
          Restangular.one('book', $stateParams.id).post('read', {
            read: $scope.book.read
          }).then(function() {
                if ($scope.book.read) {
                  $scope.book.read_date = new Date().getTime();
                } else {
                  $scope.book.read_date = null;
                }
              });
        }
      }, true);

      // Watch fav state change
      $scope.$watch('book.favourite', function(prev, next) {
        if (prev !== next) {
          // $scope.book.favourite = next ? 1 : 0;
          Restangular.one('book', $stateParams.id).post('favourite', {
            favourite: $scope.book.favourite ? 1 : 0
          }).then(function() {});
        }
      }, true);
    }, 1);
  });
});