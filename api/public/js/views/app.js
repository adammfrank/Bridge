define([

	'views/splash',
	'views/header',
	'views/form',
	'views/alerts',
	'text!../../html/app.html'
	
], function(SplashView, HeaderView, FormView, AlertsView, temp){
	
	return Backbone.View.extend({
		el: 'body',

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.render();
			var splash = new SplashView();
			var header = new HeaderView();
			var form = new FormView();
			var alerts = new AlertsView();
		},

		render: function() {
			$(this.el).html(this.template({}));
		}
	})

});