// Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

'use strict';

module.exports = function(config) {
  var autoWatch = true;

  config.plugins.push('karma-ng-html2js-preprocessor');
  config.set({
    // base path, that will be used to resolve files and exclude
    basePath: '',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [
      'src/bower_components/jquery/dist/jquery.js',
      'src/bower_components/jquery-ui/jquery-ui.js',
      'src/bower_components/angular/angular.js',
      'src/bower_components/select2/select2.js',
      'src/bower_components/ng-breadcrumbs/dist/ng-breadcrumbs.js',
      'src/bower_components/angular-animate/angular-animate.js',
      'src/bower_components/angular-ui-select2/src/select2.js',
      'src/bower_components/angular-ui-select/dist/select.js',
      'src/bower_components/angular-ui-sortable/sortable.js',
      'src/bower_components/angular-cookies/angular-cookies.js',
      'src/bower_components/angular-route/angular-route.js',
      'src/bower_components/angular-resource/angular-resource.js',
      'src/bower_components/angular-sanitize/angular-sanitize.js',
      'src/bower_components/angular-loading-bar/src/loading-bar.js',
      'src/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
      'src/bower_components/angular-placeholder-tai/lib/tai-placeholder.js',
      'src/bower_components/lodash/lodash.js',
      'src/bower_components/stacktrace/stacktrace.js',
      'src/bower_components/AngularJS-Toaster/toaster.js',
      'src/bower_components/angular-mocks/angular-mocks.js',
      'src/bower_components/angular-post-message/dist/angular-post-message.js',
      'src/bower_components/angular-tablesort/js/angular-tablesort.js',
      'src/bower_components/aituaipaljs/**/*.js',
      'src/js/**/*.js',
      'test/mock/**/*.js',
      'test/spec/**/*.js',
      'src/template/yhteiset/direktiivit/*.html'
    ],

    preprocessors: {
      '**/*.html': ['ng-html2js']
    },

    ngHtml2JsPreprocessor: {
      stripPrefix: 'src/'
    },

    // list of files / patterns to exclude
    exclude: [],

    // web server port
    port: 8000,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: autoWatch,


    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['Chrome', 'Firefox'],


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: !autoWatch,


    reporters: ['dots']
  });
};
