'use strict';

/**
 * Book view controller.
 */
App.controller('LibraryBook', function($scope, $q, $timeout, $state, $stateParams, Restangular) {

  
  // Load book
  var bookPromise = Restangular.one('library', $stateParams.id).get().then(function(data) {
    $scope.book = data;
  })

  /**
   * Update the rating of the book.
   * @param {number} rating - The rating value.
   */
  $scope.updateRating = function(rating) {
    var bookId = $stateParams.id;
    Restangular.one('library/rate', bookId).post('', { rating: rating }).then(function() {
      alert('Rating updated successfully');
    }, function (response) {
      alert('Error updating the rating: ' + response.data.message);
    }).catch(function(error) {
      alert('Error updating the rating.');
    });
  };
});