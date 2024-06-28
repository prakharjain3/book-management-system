'use strict';

/**
 * Book add controller.
 */
App.controller(
  "PodcastAdd",
  function ($scope, $http, $state, $stateParams, Restangular) {
    $scope.searchSpotify = function () {
      Restangular.one("podcast/spotify/search")
        .put({
          spotifyName: $scope.spotifyName,
        })
        .then(
          function (data) {
            $scope.spotifyResults = data.podcasts.map(function (
              result,
              index
              ) {
              return { index: index, podcast: result };
            });
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };

    $scope.addSpotify = function (index) {
      Restangular.one("podcast/spotify/add")
        .post("", $scope.spotifyResults[index].podcast)
        .then(
          function (data) {
            $state.transitionTo("podcastview", { id: data.id });
            alert("Podcast added");
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };

    $scope.searchItunes = function () {
      Restangular.one("podcast/itunes/search")
        .put({
          itunesName: $scope.itunesName,
        })
        .then(
          function (data) {
            $scope.itunesResults = data.podcasts.map(function (
              result,
              index
            ){
              return { index: index, podcast: result };
            });
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };

    $scope.addItunes= function (index) {
      Restangular.one("podcast/itunes/add")
        .post("", $scope.itunesResults[index].podcast)
        .then(
          function (data) {
            $state.transitionTo("podcastview", { id: data.id });
            alert("Podcast added");
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };
  }
);
