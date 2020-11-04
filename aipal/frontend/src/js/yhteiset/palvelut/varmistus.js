// Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
//
// This program is free software:  Licensed under the EUPL, Version 1.1 or - as
// soon as they will be approved by the European Commission - subsequent versions
// of the EUPL (the "Licence");
//
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// European Union Public Licence for more details.

'use strict';

angular.module('yhteiset.palvelut.varmistus', [])
  .factory('varmistus', ['$uibModal', '$q', function($uibModal, $q) {
    return {
      varmista: function(otsikko, alaotsikko, teksti, vahvistusnappi) {
        var deferred = $q.defer();

        var modalInstance = $uibModal.open({
          templateUrl: 'template/yhteiset/palvelut/varmistus.html',
          controller: 'VarmistusModalController',
          resolve: {
            tekstit: function() {
              return {
                otsikko: otsikko,
                alaotsikko: alaotsikko,
                teksti: teksti,
                vahvistusnappi: vahvistusnappi
              };
            }
          }
        });

        modalInstance.result.then(function() {
          deferred.resolve();
        }, function() {
          deferred.reject();
        }).catch(function (e) {
          console.error(e);
        });

        return deferred.promise;
      }
    };
  }])

  .controller('VarmistusModalController', ['$uibModalInstance', '$scope', 'i18n', 'tekstit', function($uibModalInstance, $scope, i18n, tekstit) {
    $scope.i18n = i18n;

    $scope.otsikko = tekstit.otsikko;
    $scope.alaotsikko = tekstit.alaotsikko;
    $scope.teksti = tekstit.teksti;
    $scope.vahvistusnappi = tekstit.vahvistusnappi;

    $scope.ok = $uibModalInstance.close;
    $scope.cancel = $uibModalInstance.dismiss;
  }])
;
