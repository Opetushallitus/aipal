'use strict';

module.exports = function (grunt) {

  require('load-grunt-tasks')(grunt);
  require('time-grunt')(grunt);

  var distDir = '../resources/public/app';

  function wrapLocale(req, res, next) {
    var localePattern = /^\/fi|^\/sv/;
    if(!req.url.match(localePattern)) {
      res.writeHead(302, { 'Location': '/fi/' });
      res.end();
    } else {
      req.url = req.url.replace(localePattern, '');
      next();
    }
  }

  grunt.initConfig({
    connect: {
      server: {
        options: {
          port: 3000,
          debug: true,
          base: 'src',
          middleware: function (connect, options) {
            if (!Array.isArray(options.base)) {
              options.base = [options.base];
            }

            var middlewares = [wrapLocale,
                               require('grunt-connect-proxy/lib/utils').proxyRequest];

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
          'src/template/**/*.html',
          'src/js/**/*.js',
          'src/img/**/*.{png,jpg,jpeg,gif,webp,svg}',
          'src/compiled_css/main.css'
        ],
        options: {
          livereload: true
        }
      }
    },
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
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
              'template/**/*.html',
              'img/**/*.{png,jpg,jpeg,gif,webp,svg}',
              'font/**/*'],
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
