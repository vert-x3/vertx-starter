/*
 * Copyright (c) 2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
angular
.module('app', ['ngResource', 'ui.bootstrap', 'cfp.hotkeys'])
.value('bowser', bowser)
.config(['hotkeysProvider', function(hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = false;
}])
.factory('Version', ['$resource', function($resource) {
    var service = $resource('/versions/:version', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        }
    });

    return service;
}])
.factory('Dependency', ['$resource', function($resource) {
    var service = $resource('/dependencies/:dependency', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        }
    });

    return service;
}])
.factory('Starter', ['$resource', function($resource) {
    var service = $resource('/starter.zip', {}, {
        'starter': {
             method:'GET',
             responseType: 'arraybuffer',
             cache: false,
             transformResponse: function (data, headers) {
                //https://github.com/eligrey/FileSaver.js/issues/156#issuecomment-250858896
                var response = {};
                response.data = data;
                return response;
             }
         }
    });

    return service;
}])
.controller('VertxStarterController', ['$document', '$window','hotkeys', 'Version', 'Dependency', 'Starter',
function VertxStarterController($document, $window, hotkeys, Version, Dependency, Starter) {
    var vm = this;

    vm.versions= [];
    vm.dependencies =  [];
// Not used right now
//    vm.languages =  ['Java','Javascript','Groovy','Ruby','Ceylon','Kotlin','Scala'];
//    vm.buildTools = ['Maven','Gradle'];
    vm.languages =  ['Java','Kotlin'];
    vm.buildTools = ['Maven', 'Gradle'];

    vm.project = {
        version: null,
        language: 'Java',
        build: 'Maven',
        groupId: 'com.example',
        artifactId: 'demo',
        dependencies: []
    };
    vm.selectedDependency = null;
    vm.onDependencySelected = onDependencySelected;
    vm.removeDependency = removeDependency;
    vm.generate = generate;

    loadAll();

    hotkeys.add({
        combo: ['command+enter', 'alt+enter'],
        callback: function(event, hotkey) {
            event.preventDefault();
            generate();
        }
    });
    vm.hotkey = (bowser.mac) ? '\u2318 + \u23CE' : 'alt + \u23CE';

    try {
        vm.isFileSaverSupported = !!new Blob;
    } catch (e) {}

    function loadAll() {
        getVersions();
        getDependencies();
    }

    function getVersions() {
        Version.query({}, function(versions) {
            vm.versions = versions;
            vm.project.version = vm.versions[0];
        }, function(error) {
            console.error('Impossible to load versions. ' + JSON.stringify(error));
        });
    }


    function getDependencies() {
        Dependency.query({}, function(dependencies) {
            vm.dependencies = dependencies
            .map(function (category ) {
                return category.items;
            }).reduce(function (a, b) {
                return a.concat(b);
            });
        }, function(error) {
            console.error('Impossible to load dependencies. ' + JSON.stringify(error));
        });
    }

    function onDependencySelected($item, $model, $label, $event) {
        addDependency($model);
        vm.selectedDependency = null;
    }

    function indexOfDependency(predicate) {
        for (var i = 0; i < vm.project.dependencies.length; i++ ) {
            if (predicate(vm.project.dependencies[i])) {
                return i;
            }
        }
        return -1;
    }

    function addDependency(dependency) {
        var index = indexOfDependency(function(it) {
            return it.name === dependency.name;
        });
        if (index == -1) {
            vm.project.dependencies.push(dependency);
        }
    }

    function removeDependency(dependency) {
        vm.project.dependencies = vm.project.dependencies.filter(function(it) {
            return it.name !== dependency.name;
        });
    }

    function save(data) {
        if(vm.isFileSaverSupported) {
            var archive = new Blob([data], {type: 'application/octet-stream'});
            saveAs(archive, vm.project.artifactId + '.zip');
        }
    }


    function projectRequest() {
        var project = {};
        angular.copy(vm.project, project);
        var artifacts = vm.project.dependencies.map(function(dependency) {
            return dependency.artifactId;
        });
        project.dependencies = artifacts.join();
        return project;
    }

    function generate() {
        Starter
        .starter(projectRequest(), function(response) {
            save(response.data);
        }, function(error) {
            console.error('Impossible to create project. ' + JSON.stringify(error));
        });
    };

}]);
