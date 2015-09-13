define([
	
	'text!../../html/map.html'
	
], function(temp) {

	return Backbone.View.extend({
		initialize: function(options) {
			this.template = Handlebars.compile(temp);
			this.id = options.id;
			this.render()
		},

		render: function() {
			$('#map-container-' + this.id).html(this.template({}));
		}
	});

});