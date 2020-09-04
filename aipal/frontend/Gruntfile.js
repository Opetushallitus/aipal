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

'use strict';

module.exports = function (grunt) {

  const sass = require('node-sass');
  require('load-grunt-tasks')(grunt);
  require('time-grunt')(grunt);

  var distDir = '../resources/public/app';

  grunt.initConfig({
    connect: {
      server: {
        options: {
          port: 3002,
          debug: true,
          base: 'src',
          middleware: function (connect, options) {
            if (!Array.isArray(options.base)) {
              options.base = [options.base];
            }

            var middlewares = [require('grunt-connect-proxy/lib/utils').proxyRequest];

            options.base.forEach(function(base) {
              middlewares.push(connect.static(base));
            });

            var directory = options.directory || options.base[options.base.length - 1];
            middlewares.push(connect.directory(directory));

            return middlewares;
          }
        },
        proxies: [{
          context: '/api',
          host: 'localhost',
          port: 8082
        }]
      }
    },
    watch: {
      sass: {
        files: ['src/sass/**/*.scss'],
        tasks: ['sass:compile']
      },
      livereload: {
        files: [
          'src/index.html',
          'src/status.html',
          'src/template/**/*.html',
          'src/js/**/*.js',
          'src/img/**/*.{png,jpg,jpeg,gif,webp,svg}',
          'src/compiled_css/main.css'
        ],
        options: {
          livereload: 35731
        }
      }
    },
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish'),
        reporterOutput: ''
      },
      all: [
        'Gruntfile.js',
        'src/js/**/*.js'
      ],
      test: {
        options: {
          jshintrc: 'test/.jshintrc'
        },
        src: ['test/spec/**/*.js']
      }
    },
    karma: {
      unit: {
        configFile: 'karma.conf.js',
        autoWatch: false,
        singleRun: true
      },
      unit_ff: {
        configFile: 'karma.conf.js',
        autoWatch: false,
        singleRun: true,
        browsers: ['Firefox'],
        colors: false
      },
      unit_auto: {
        configFile: 'karma.conf.js'
      }
    },
    sass: {
      options: {
        implementation: sass
      },
      compile : {
        files: {
          'src/compiled_css/main.css': 'src/sass/main.scss'
        }
      },
      dist : {
        files: {
          '../resources/public/app/compiled_css/main.css': 'src/sass/main.scss'
        }
      }
    },
    clean: {
      files : [distDir],
      options: {force : true}
    },
    useminPrepare: {
      html: 'src/index.html',
      options: {
        dest: distDir,
        flow : {
          steps: {
            'css': ['concat'],
            'js': ['concat']
          },
          post: {}
        }
      }
    },
    copy: {
      dist : {
        expand : true,
        cwd: 'src',
        src: ['index.html',
              'status.html',
              'template/**/*.html',
              'img/**/*.{png,jpg,jpeg,gif,webp,svg}',
              'font/**/*',
              'css/**/*'],
        dest: distDir,
        options : {
          noProcess: '**/*.{png,gif,jpg,ico,svg,woff}',
          process: function (content) {
            return content.replace(/<!--dev-->.*<!--enddev-->/g, '')
              .replace(/<!-- mustache/g, '')
              .replace(/end mustache -->/g, '');
          }
        }
      },
      dist_angular_min_map : {
        expand : true,
        cwd: 'src',
        src : ['bower_components/angular/angular.min.js.map',
               'bower_components/angular-route/angular-route.min.js.map',
               'bower_components/angular-resource/angular-resource.min.js.map',
               'bower_components/jquery/dist/jquery.min.map'],
        dest: distDir + '/js',
        flatten : true
      }
    },
    usemin: {
      html: [distDir + '/index.html']
    }
  });

  grunt.registerTask('test', ['jshint', 'karma:unit']);

  grunt.registerTask('test_ff', ['jshint', 'karma:unit_ff']);

  grunt.registerTask('autotest', ['karma:unit_auto']);

  grunt.registerTask('default',
    ['sass:compile',
     'configureProxies:server',
     'connect:server',
     'watch']);

  grunt.registerTask('build',
    ['clean',
     'sass:dist',
     'useminPrepare',
     'concat',
     'copy:dist',
     'copy:dist_angular_min_map',
     'usemin']);
};
