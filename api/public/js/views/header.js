define([

	'text!../../html/header.html'
	
], function(temp){

	return Backbone.View.extend({
		el: '.header',

		events: {
			'click .link': 'nav'
		},

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.render();
		},

		render: function() {
			$(this.el).html(this.template({}));
		},

		nav: function(e) {
			var rel = $(e.target).attr('rel');
			if (rel == 'home') {
				$('.splash').removeClass('hidden');
				return false;
			}
		}
	});

});