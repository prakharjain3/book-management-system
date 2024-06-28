"use strict";

/**
 * Book add controller.
 */
App.controller(
  "AudiobookAdd",
  function ($scope, $http, $state, $stateParams, Restangular) {
    $scope.searchSpotify = function () {
      Restangular.one("audiobook/spotify/search")
        .put({
          spotifyName: $scope.spotifyName,
        })
        .then(
          function (data) {
            $scope.spotifyResults = data.audiobooks.map(function (
              result,
              index
            ) {
              return { index: index, audiobook: result };
            });
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };

    $scope.addSpotify = function (index) {
      // alert('Index: ' + index);
      Restangular.one("audiobook/spotify/add")
        .post("", $scope.spotifyResults[index].audiobook)
        .then(
          function (data) {
            $state.transitionTo("audiobookview", { id: data.id });
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };

    $scope.searchItunes = function () {
      Restangular.one("audiobook/itunes/search")
        .put({
          itunesName: $scope.itunesName,
        })
        .then(
          function (data) {
            $scope.itunesResults = data.audiobooks.map(function (
              result,
              index
            ){
              return { index: index, audiobook: result };
            });
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };

    $scope.addItunes= function (index) {
      Restangular.one("audiobook/itunes/add")
        .post("", $scope.itunesResults[index].audiobook)
        .then(
          function (data) {
            $state.transitionTo("audiobookview", { id: data.id });
            alert("Audiobook added");
          },
          function (response) {
            alert(response.data.message);
          }
        );
    };
  }
);
