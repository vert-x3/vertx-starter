/*
 * Copyright (c) 2017-2018 Daniel Petisme
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular
  .module('app', ['ngResource', 'ui.bootstrap', 'cfp.hotkeys'])
  .value('bowser', bowser)
  .factory('Starter', ['$http', function ($http) {
    var service = {
      'generate': function(vertxProject) {
        return $http.get('/starter.' + vertxProject.archiveFormat, {
          responseType: 'blob',
          params: {
            type: vertxProject.type,
            groupId: vertxProject.groupId,
            artifactId: vertxProject.artifactId,
            language: vertxProject.language,
            buildTool: vertxProject.buildTool,
            vertxVersion: vertxProject.vertxVersion,
            vertxDependencies: vertxProject.vertxDependencies
          }
        });
      },
      'metadata': function() {
        return $http.get('/metadata');
      }
    };

    return service;
  }])
  .factory('httpErrorInterceptor', ['$q', function($q) {
    return {
      'responseError': function(rejection) {
        if (rejection.status === 500) {
            var errorData = rejection.data;
            var decoder = new TextDecoder("utf-8");
            rejection.data = JSON.parse(decoder.decode(errorData.data));
        }
        return $q.reject(rejection);
      }
    }
  }])
  .filter('capitalize', function() {
      return function(input) {
        return (!!input) ? input.charAt(0).toUpperCase() + input.slice(1).toLowerCase() : '';
      }
  })
  .config(['$httpProvider', 'hotkeysProvider', function ($httpProvider, hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = false;
    $httpProvider.interceptors.push('httpErrorInterceptor');
  }])
  .controller('VertxStarterController', ['$document', '$window', 'hotkeys', 'Starter',
    function VertxStarterController($document, $window, hotkeys, Starter) {
      var vm = this;
      vm.isGenerating = false
      vm.vertxVersions = [];
      vm.vertxDependencies = [];
      vm.languages = [];
      vm.buildTools = [];

      vm.vertxProject = {};
      vm.selectedDependency = null;
      vm.alerts = [];
      vm.onDependencySelected = onDependencySelected;
      vm.removeDependency = removeDependency;
      vm.generate = generate;
      vm.addAlert = addAlert;
      vm.closeAlert = closeAlert;

      loadAll();

      hotkeys.add({
        combo: ['command+enter', 'alt+enter'],
        callback: function (event, hotkey) {
          event.preventDefault();
          generate();
        }
      });
      vm.hotkey = (bowser.mac) ? '\u2318 + \u23CE' : 'alt + \u23CE';

      try {
        vm.isFileSaverSupported = !!new Blob;
      } catch (e) {
      }

      function loadAll() {
        Starter.metadata()
          .then(function(response) {
            var data = response.data;
            vm.vertxVersions = data.vertxVersions.slice(0, 10);
            vm.vertxDependencies = data.vertxDependencies
              .map(function (category) {
                return category.items;
              }).reduce(function (a, b) {
                return a.concat(b);
              });
            vm.buildTools = data.buildTools;
            vm.languages = data.languages;
            initProjectWithDefaults(data.defaults);
          })
          .catch(function(error) {
            console.error('Impossible to load starter metadata: ' + JSON.stringify(error));
          });
      }

      function initProjectWithDefaults(defaults) {
        vm.vertxProject.type = defaults.type;
        vm.vertxProject.model = defaults.model;
        vm.vertxProject.groupId = defaults.groupId;
        vm.vertxProject.artifactId = defaults.artifactId;
        vm.vertxProject.language = defaults.language;
        vm.vertxProject.buildTool = defaults.buildTool;
        vm.vertxProject.vertxVersion = defaults.vertxVersion;
        vm.vertxProject.archiveFormat = defaults.archiveFormat;
        vm.vertxProject.vertxDependencies = [];
      }

      function onDependencySelected($item, $model, $label, $event) {
        addDependency($model);
        vm.selectedDependency = null;
      }

      function indexOfDependency(predicate) {
        for (var i = 0; i < vm.vertxProject.vertxDependencies.length; i++) {
          if (predicate(vm.vertxProject.vertxDependencies[i])) {
            return i;
          }
        }
        return -1;
      }

      function addDependency(dependency) {
        var index = indexOfDependency(function (it) {
          return it.name === dependency.name;
        });
        if (index == -1) {
          vm.vertxProject.vertxDependencies.push(dependency);
        }
      }

      function removeDependency(dependency) {
        vm.vertxProject.vertxDependencies = vm.vertxProject.vertxDependencies.filter(function (it) {
          return it.name !== dependency.name;
        });
      }

      function save(data, contentType) {
        if (vm.isFileSaverSupported) {
          var archive = new Blob([data], {type: contentType});
          console.log(archive.size);
          saveAs(archive, vm.vertxProject.artifactId + '.' + vm.vertxProject.archiveFormat);
        }
      }


      function vertxProjectRequest() {
        var vertxProject = {};
        angular.copy(vm.vertxProject, vertxProject);
        var artifacts = vm.vertxProject.vertxDependencies.map(function (dependency) {
          return dependency.artifactId;
        });
        vertxProject.vertxDependencies = artifacts.join();
        console.log(JSON.stringify(vertxProject));
        return vertxProject;
      }

      function generate() {
        vm.isGenerating = true
        Starter
          .generate(vertxProjectRequest())
          .then(function (response) {
            vm.isGenerating = false
            var headers = response.headers()
            save(response.data, headers['Content-Type']);
          })
          .catch(function(error) {
             vm.isGenerating = false
             addAlert(error.data.message);
          });
      };

      function addAlert(message) {
        vm.alerts.push(message);
      };

      function closeAlert(index) {
        vm.alerts.splice(index, 1);
      };

    }]);
