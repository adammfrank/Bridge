define([

	'views/results',
	'text!../../html/form.html'
	
], function(ResultsView, temp){

	return Backbone.View.extend({
		el: '#form',

		events: {
			'click a[rel=submit-name]': 'submit_name',
			'click a[rel=submit-license]': 'submit_license'
		},

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.results = new ResultsView();
			this.render();
		},

		render: function() {
			$(this.el).html(this.template({}));
		},

		populate: function(data) {
			if (!data.citations.length && !data.violations.length && !data.warrants.length && !data.combined.length)
				data['empty'] = true;
			if (data.combined.length) {
				_.each(data.combined, function(comb) {
					var id = comb.citation_number.N;
					data.citations = _.filter(data.citations, function(c) {
						return c.citation_number.N != id;
					});
					data.violations = _.filter(data.violations, function(v) {
						return v.citation_number.N != id;
					});
				});
			}
			this.results.populate({data: data});
		},

		submit_name: function() {
			$('#form input').removeClass('fix');
			var fname = $('input[name=fname]').val();
			var lname = $('input[name=lname]').val();
			var dob   = $('input[name=dob]').val();
			var to_fix = [];
			if (!fname) to_fix.push('fname');
			if (!lname) to_fix.push('lname');
			if (!dob) to_fix.push('dob');
			if (to_fix.length) {
				_.each(to_fix, function(field) {
					$('#form input[name=' + field + ']').addClass('fix');
				});
				toastr.error('Please fill in required fields');
			}
			else if (!this.check_dob(dob)) {
				$('#form input[name=dob]').addClass('fix');
				toastr.error('Date of birth should be formatted as dd/mm/yyyy');
			}
			else {
				fname = fname.toLowerCase();
				lname = lname.toLowerCase();
				var ident = {
					"fname": fname,
					"lname": lname,
					"dob": dob
				};

				$.ajax({
					url: "/dataRequestWithName",
					method: "POST",
					data: ident
				}).success(function(data){
					this.populate(data);
				}.bind(this)).fail(function(err){
					console.log(err);
				});

				var data = {
					court: 'The Court',
					addr: '123 Court Street',
					site: 'https://www.payfines.com',
					phone: '413-suck-a-dick-dumshits',
					results: [
						{
							id: 1,
							title: 'Warrant 1',
							type: 'Resisting Arrest',
							desc: 'Class B Felony punishible by 2- 10 years in prison. Perp can not live within two miles of a school or play-gorund after serving the sentence.',
							stats: [
								{chart: 'lawyer', caption: 'Percentage of people who hired a lawyer', data: [
									{ title: 'Yes', value : 75,  color: "#23a3e8" },
			    					{ title: 'No', 	value:  25,  color: "#e75b51" },
			    				]},
								{chart: 'pleas', caption: 'Percentage of pleas in court', data: [
									{ title: 'Guilty', value : 50,  color: "#23a3e8" },
			    					{ title: 'Not Guilty', 	value:  25,  color: "#e07732" },
			    					{ title: 'No Contest', 	value:  25,  color: "#e75b51" },
			    				]}
			    			],
							fine: '$2'
						},
						{
							id: 2,
							title: 'Warrant 2',
							type: 'Resisting Arrest',
							desc: 'Class B Felony punishible by 2- 10 years in prison. Perp can not live within two miles of a school or play-gorund after serving the sentence.',
							stats: [
								{chart: 'lawyer', caption: 'Percentage of people who hired a lawyer', data: [
									{ title: 'Yes', value : 75,  color: "#23a3e8" },
			    					{ title: 'No', 	value:  25,  color: "#e75b51" },
			    				]},
								{chart: 'pleas', caption: 'Percentage of pleas in court', data: [
									{ title: 'Guilty', value : 50,  color: "#23a3e8" },
			    					{ title: 'Not Guilty', 	value:  25,  color: "#e75b51" },
			    					{ title: 'No Contest', 	value:  25,  color: "#e07732" },
			    				]}
			    			],
							fine: '$2'
						},
						{
							id: 3,
							title: 'Citation 1',
							type: 'Resisting Arrest',
							desc: 'Class B Felony punishible by 2- 10 years in prison. Perp can not live within two miles of a school or play-gorund after serving the sentence.',
							stats: [
								{chart: 'lawyer', caption: 'Percentage of people who hired a lawyer', data: [
									{ title: 'Yes', value : 75,  color: "#23a3e8" },
			    					{ title: 'No', 	value:  25,  color: "#e75b51" },
			    				]},
								{chart: 'pleas', caption: 'Percentage of pleas in court', data: [
									{ title: 'Guilty', value : 50,  color: "#23a3e8" },
			    					{ title: 'Not Guilty', 	value:  25,  color: "#e75b51" },
			    					{ title: 'No Contest', 	value:  25,  color: "#e07732" },
			    				]}
			    			],
							fine: '$2'
						}
					]
				};
				//this.populate(data);
			}
			return false;
		},

		check_dob: function(dob) {
			dob = dob.split('/');
			if (dob.length != 3) return false;
			if (isNaN(dob[0])) return false;
			if (isNaN(dob[1])) return false;
			if (isNaN(dob[2])) return false;
			if (dob[0].length != 2) return false;
			if (dob[1].length != 2) return false;
			if (dob[2].length != 4) return false;
			return true;
		},

		submit_license: function() {
			$('#record input').removeClass('fix');
			var license = $('input[name=license]').val();
			if (!license) {
				$('input[name=license]').addClass('fix');
				toastr.error('Please fill in required fields');
			}
			else {
				console.log(license);
			}
			return false;
		}
	});

});