define([

	'text!../../html/splash.html'
	
], function(temp){

	return Backbone.View.extend({
		el: '.splash',

		events: {
			'click a[rel=go]': 'go'
		},

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.render();
		},

		render: function() {
			$(this.el).html(this.template({foo: 'bar'}));
		},

		go: function(e) {
			$(this.el).addClass('hidden');
			return false;
		},
	});

});