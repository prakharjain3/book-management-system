'use strict';

/**
 * Book view controller.
 */
App.controller('AudiobookView', function($scope, $q, $timeout, $state, $stateParams, Restangular) {
  
  // Load book
  var bookPromise = Restangular.one('audiobook', $stateParams.id).get().then(function(data) {
    $scope.book = data;
  });

  $q.all([bookPromise]).then(function() {
    $timeout(function() {
      $scope.loading = false;

      if($scope.book.language == 'English' || $scope.book.language == 'USA' || $scope.book.language.includes('en')){
        $scope.book.language = 'en'
      }else if ($scope.book.language == 'Spanish'){
        $scope.book.language = 'es'
      }else if ($scope.book.language == 'French'){
        $scope.book.language = 'fr'
      }

      $scope.book.favourite = $scope.book.favourite == 1;

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