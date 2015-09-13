define([
	
	'text!../../html/splash.html'
	
], function(temp){

	return Backbone.View.extend({
		el: '.splash',

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.render();
		},

		render: function() {
			$(this.el).html(this.template({foo: 'bar'}));
		}
	});

});