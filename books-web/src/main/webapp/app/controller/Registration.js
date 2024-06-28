'use strict';

/**
 * Settings user edition page controller.
 */
App.controller('Registration', function($scope, $state, User, Restangular) {

  $scope.register = function() {
    Restangular.one('user/add').put($scope.user).then(function(response) {
      $state.transitionTo('login');
    }).catch(function(error) {
      $scope.errorMessage = 'Failed to add user. Please try again.';
    });
  };
});