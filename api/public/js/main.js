require.config({

  paths: {
    jquery: 'libs/jquery/jquery.min',
    underscore: 'libs/underscore/underscore.min',
    handlebars: 'libs/handlebars/handlebars.min',
    backbone: 'libs/backbone/backbone.min',
    toastr: 'libs/toastr/toastr.min'
  }

});

define([

	'jquery',
	'underscore',
	'handlebars',
	'backbone',
	'toastr',
	'views/app'

], function($, _, Handlebars, Backbone, toastr, AppView){
	$(function() {
		window.$ = $;
		window._ = _;
		window.Handlebars = Handlebars;
		window.Backbone = Backbone;
		window.toastr = toastr;
		window.toastr.options.preventDuplicates = true;
		return new AppView();
	});
});