define([
	
	'views/chart',
	'views/map',
	'text!../../html/results.html'
	
], function(ChartView, MapView, temp){

	return Backbone.View.extend({
		el: '#results',

		events: {
			'click .toggle img': 'toggle'
		},

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.lawyer_chart = new ChartView();
			this.pleas_chart = new ChartView();
		},

		render: function() {
			$(this.el).html(this.template(this.data.data));
		},

		populate: function(data) {
			this.data = data;
			this.render();
		},

		toggle: function(e) {
			var rel = $(e.target).parent('span').attr('rel');
			var li = $(e.target).parents('#' + rel);
			if ($(li).hasClass('open')) {
				$(li).children('.dropdown').hide();
				$(li).removeClass('open');
			}
			else {
				$('#results li').children('.dropdown').hide();
				$('#results li').removeClass('open');
				$(li).children('.dropdown').slideToggle(100);
				$(li).addClass('open');
				$(li).find('.chart').html('');
				$(li).find('div[id^=map-container]').html('');

				// Dummy data
				var stats = [
					{chart: 'lawyer', caption: 'Percentage of people who hired a lawyer', data: [
						{ title: 'Yes', value : 75,  color: "#23a3e8" },
    					{ title: 'No', 	value:  25,  color: "#e75b51" },
    				]},
					{chart: 'pleas', caption: 'Percentage of pleas in court', data: [
						{ title: 'Guilty', value : 50,  color: "#23a3e8" },
    					{ title: 'Not Guilty', 	value:  25,  color: "#e07732" },
    					{ title: 'No Contest', 	value:  25,  color: "#e75b51" },
    				]}
    			]

				_.each(stats, function(stat) {
					var chart_id = '#' + stat.chart + '-' + rel;
					var options = {chart: chart_id, caption: stat.caption, data: stat.data}
					if (stat.chart == 'lawyer')
						this.lawyer_chart.render(options);
					else
						this.pleas_chart.render(options);
					var map = new MapView({id:rel});
				}.bind(this));
			}	
		} 
	});

});