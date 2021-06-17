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
String.prototype.format = String.prototype.format ||
function () {
    "use strict";
    var str = this.toString();
    if (arguments.length) {
        var t = typeof arguments[0];
        var key;
        var args = ("string" === t || "number" === t) ?
            Array.prototype.slice.call(arguments)
            : arguments[0];

        for (key in args) {
            str = str.replace(new RegExp("\\${" + key + "\\}", "gi"), args[key]);
        }
    }

    return str;
};

angular
  .module('app', ['ngResource', 'ngAnimate', 'ui.bootstrap', 'cfp.hotkeys',  'ngclipboard'])
  .value('bowser', bowser)
  .factory('Starter', ['$http', function ($http) {
    var service = {
      'generate': function (vertxProject) {
        return $http.get('/starter.' + vertxProject.archiveFormat, {
          responseType: 'blob',
          params: {
            groupId: vertxProject.groupId,
            artifactId: vertxProject.artifactId,
            language: vertxProject.language,
            buildTool: vertxProject.buildTool,
            vertxVersion: vertxProject.vertxVersion,
            vertxDependencies: vertxProject.vertxDependencies,
            packageName: vertxProject.packageName,
            jdkVersion: vertxProject.jdkVersion,
            flavor: vertxProject.flavor
          }
        });
      },
      'metadata': function () {
        return $http.get('/metadata');
      }
    };

    return service;
  }])
  .constant('CliConstants', {
    commands: {
      curl: {
        name: "curl",
        template: "curl -G ${baseUrl}starter.${archiveFormat} ${args} --output ${artifactId}.${archiveFormat}",
        argMapper: function (name, value) { return "-d \"${0}=${1}\"".format(name, value); },
        argSeparator: " "
      },
      httpie: {
        name: "HTTPie",
        template: "http ${baseUrl}starter.${archiveFormat} ${args} --output ${artifactId}.${archiveFormat}",
        argMapper: function (name, value) { return "${0}==${1}".format(name, value); },
        argSeparator: " "
      },
      powershell: {
        name: "Power Shell",
        template: "Invoke-WebRequest -Uri ${baseUrl}starter.${archiveFormat} -Body @{ ${args} } -OutFile ${artifactId}.${archiveFormat}",
        argMapper: function (name, value) { return "${0}='${1}'".format(name, value); },
        argSeparator: "; "
      }
    },
    args: ["groupId", "artifactId", "packageName", "vertxVersion", "vertxDependencies", "language", "jdkVersion", "buildTool", "flavor"]
   })
  .filter('capitalize', function () {
    return function (input) {
      return (!!input) ? input.charAt(0).toUpperCase() + input.slice(1).toLowerCase() : '';
    }
  })
  .config(['hotkeysProvider', function (hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = false;
  }])
  .controller('VertxStarterController', ['$scope', '$document', '$location', '$window', 'hotkeys', 'Starter', 'CliConstants',
    function VertxStarterController($scope, $document, $location, $window, hotkeys, Starter, CliConstants) {
      var vm = this;
      vm.idRegexp = new RegExp('^[A-Za-z0-9_\\-.]+$');
      vm.packageNameRegexp = new RegExp('^[A-Za-z0-9_\\-.]+$');
      vm.isGenerating = false;
      vm.vertxVersions = [];
      vm.languages = [];
      vm.buildTools = [];
      vm.jdkVersions = [];
      vm.flavors = [];
      vm.advancedCollapsed = true;
      vm.isWindows = bowser.windows;

      vm.projectDefaults = {};
      vm.vertxProject = {};
      vm.alerts = [];

      vm.stack = [];
      vm.totalPackagesAvailable = 0;
      vm.detailedOptionsCollapsed = true;
      vm.selectedDependency = null;
      vm.selectedPanel = '';
      vm.availableVertxDependencies = [];

      vm.onVertxVersionChanged = onVertxVersionChanged;
      vm.onDependencySelected = onDependencySelected;
      vm.removeDependency = removeDependency;
      vm.disableDependencies = disableDependencies;
      vm.isDependencyNotAvailable = isDependencyNotAvailable;
      vm.toggleDetailedOptions = toggleDetailedOptions;
      vm.toggleAdvanced = toggleAdvanced;
      vm.generate = generate;
      vm.addAlert = addAlert;
      vm.closeAlert = closeAlert;
      vm.generateCommands = generateCommands;

      var baseUrl = $location.$$absUrl.replace($location.$$url, '');


      loadAll();

      hotkeys.add({
        combo: ['command+enter', 'alt+enter'],
        callback: function (event, hotkey) {
          event.preventDefault();
          if ($scope.form.$valid) {
            generate();
          }
        }
      });
      vm.hotkey = (bowser.mac) ? '\u2318 + \u23CE' : 'alt + \u23CE';

      try {
        vm.isFileSaverSupported = !!new Blob;
      } catch (e) {
      }

      function loadAll() {
        Starter.metadata()
          .then(function (response) {
            var data = response.data;
            initModel(data);
            initProjectWithDefaults(data.defaults);
          })
          .catch(function (error) {
            console.error('Impossible to load starter metadata: ' + JSON.stringify(error));
          });
      }

      function initModel(data) {
        vm.versions = data.versions;
        vm.exclusions = data.versions.reduce(function (res, value) {
          res[value.number] = value.exclusions || [];
          return res;
        }, {});
        vm.stack = data.stack;
        vm.buildTools = data.buildTools;
        vm.languages = data.languages;
        vm.jdkVersions = data.jdkVersions;
        vm.flavors = data.flavors;
        vm.selectedPanel = data.stack && data.stack.length > 0 ? data.stack[0].code : 'none';
        vm.vertxVersions = data.versions.map(function (version) {
          return version.number;
        });
      }

      function initProjectWithDefaults(defaults) {
        vm.projectDefaults = defaults;
        vm.vertxProject.groupId = defaults.groupId;
        vm.vertxProject.artifactId = defaults.artifactId;
        vm.vertxProject.language = defaults.language;
        vm.vertxProject.buildTool = defaults.buildTool;
        vm.vertxProject.vertxVersion = defaults.vertxVersion;
        vm.vertxProject.archiveFormat = defaults.archiveFormat;
        vm.vertxProject.vertxDependencies = [];
        vm.vertxProject.packageName = "";
        vm.vertxProject.jdkVersion = defaults.jdkVersion;
        vm.vertxProject.flavor = defaults.flavor;

        vm.disableDependencies(defaults.vertxVersion);
        refreshAvailableDependencies(defaults.vertxVersion, false);
      }

      function refreshAvailableDependencies(vertxVersion, dependenciesSelected) {
        // filter dependencies by version
        var filteredByVersion = vm.stack.flatMap(function (category) {
          return category.items.filter(function (value) {
            return !vm.exclusions[vertxVersion].includes(value.artifactId);
          });
        });

        // set total number of available dependencies for given version
        vm.totalPackagesAvailable = filteredByVersion.length;

        if (!dependenciesSelected)
          // if no dependencies has been selected yet return all of them
          vm.availableVertxDependencies = filteredByVersion;
        else
          // if some of the dependencies has been already selected filter also by already selected dependencies so they are not available in typeahead
          vm.availableVertxDependencies = filteredByVersion.filter(function (value) {
          return !vm.vertxProject.vertxDependencies.includes(value)
        })
      }

      function onDependencySelected($item, $model, $label, $event) {
        var index = indexOfDependency(function (it) {
          return it.name === $model.name;
        });
        if (index === -1) {
          addDependency($model);
        } else {
          vm.removeDependency($model.artifactId);
        }
        vm.selectedDependency = null;
      }

      function onVertxVersionChanged() {
        var version = vm.vertxProject.vertxVersion;
        vm.exclusions[version].forEach(removeDependency);
        vm.disableDependencies(version);

        // refresh dependencies so there are only available dependencies for given version
        refreshAvailableDependencies(version)
      }

      function disableDependencies(version) {
        vm.stack.forEach(function (cat) {
          cat.items.forEach(function (dep) {
            dep.disabled = isDependencyNotAvailable(dep, version);
          })
        })
      }

      function isDependencyNotAvailable(dependency, version) {
        return vm.exclusions[version].includes(dependency.artifactId);
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
        dependency.selected = true;
        vm.vertxProject.vertxDependencies.push(dependency);

        // refresh available dependencies so their are not available in typeahead anymore
        refreshAvailableDependencies(vm.vertxProject.vertxVersion)
      }

      function removeDependency(artifactId) {
        // filter out given dependency
        vm.vertxProject.vertxDependencies = vm.vertxProject.vertxDependencies.filter(function (it) {
          return it.artifactId !== artifactId;
        });

        // deselect (set selected = false) given dependency
        vm.stack.flatMap(function (category) {
          return category.items.filter(function (value) {
            return value.artifactId == artifactId;
          });
        }).forEach(function (module) {
          module.selected = false;
        });

        // refresh available dependencies so their are again available in typeahead
        refreshAvailableDependencies(vm.vertxProject.vertxVersion)
      }

      function save(data, contentType) {
        if (vm.isFileSaverSupported) {
          var archive = new Blob([data], {type: contentType});
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
        return vertxProject;
      }

      function toggleDetailedOptions() {
        vm.detailedOptionsCollapsed = !vm.detailedOptionsCollapsed;
        if (vm.detailedOptionsCollapsed) {
          scrollTo("homeAnchor");
        } else {
          scrollTo("dependencyTypeaheadAnchor");
        }
      }

      function toggleAdvanced() {
        if (vm.advancedCollapsed) {
          vm.advancedCollapsed = false;
          scrollTo("advancedAnchor")
        } else {
          vm.vertxProject.packageName = vm.projectDefaults.packageName;
          vm.vertxProject.jdkVersion = vm.projectDefaults.jdkVersion;
          vm.advancedCollapsed = true;
          if (vm.detailedOptionsCollapsed) {
            scrollTo("homeAnchor");
          } else {
            scrollTo("dependencyTypeaheadAnchor")
          }
        }
      }

      function generate() {
        vm.isGenerating = true;
        Starter
          .generate(vertxProjectRequest())
          .then(function (response) {
            vm.isGenerating = false;
            var headers = response.headers();
            save(response.data, headers['Content-Type']);
          })
          .catch(function (error) {
            vm.isGenerating = false;
            var reader = new FileReader();
            reader.addEventListener("loadend", function () {
              var message = JSON.parse(reader.result).message;
              $scope.$apply(function () {
                $scope.vm.addAlert(message, error.status === 400?'warning':'danger');
              });
            });
            reader.readAsText(error.data, "utf8");
          });
      }

      function addAlert(message, type) {
        vm.alerts.push({message: message, type: type});
      }

      function closeAlert(index) {
        vm.alerts.splice(index, 1);
      }

      function generateCommands(open) {
        if(!open) {
          return;
        }
        var projectRequest = vertxProjectRequest();
        if(bowser.windows) {
          vm.powershellCommand = renderCommand(CliConstants.commands.powershell, CliConstants.args, projectRequest);
        }
        vm.curlCommand = renderCommand(CliConstants.commands.curl, CliConstants.args, projectRequest);
        vm.httpieCommand = renderCommand(CliConstants.commands.httpie, CliConstants.args, projectRequest);
      }

      function renderCommand(command, commandArgs,  projectRequest) {
        return command.template.format({
           baseUrl: baseUrl,
           archiveFormat: projectRequest.archiveFormat,
           args: toCliArgs(projectRequest, commandArgs, command.argMapper, command.argSeparator),
           artifactId: projectRequest.artifactId
        });
      }

      function toCliArgs(obj, commandArgs, argMapper, argSeparator) {
        var args = [];
        commandArgs.forEach(function(argName) {
          if(obj.hasOwnProperty(argName) && obj[argName] != undefined && obj[argName].trim().length > 0 ) {
             args.push(argMapper(argName, obj[argName]));
          }
        });
        return args.join(argSeparator);
      }

      function scrollTo(elementId) {
        setTimeout(function () {
          var elmnt = document.getElementById(elementId);
          if(elmnt)
            elmnt.scrollIntoView({block: "start", inline: "nearest", behavior: "smooth"});
        }, 150)
      }
    }]);
