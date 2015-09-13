define([
	
	'text!../../html/alerts.html'
	
], function(temp){

	return Backbone.View.extend({
		el: '#alerts',

		events: {
			'click a[rel=submit-number]': 'submit'
		},

		initialize: function() {
			this.template = Handlebars.compile(temp);
			this.render();
		},

		render: function() {
			$(this.el).html(this.template({}));
		},

		submit: function() {
			$('#alerts input').removeClass('fix');
			var social = $('input[name=social]').val();
			var number = $('input[name=number]').val();
			var fname = $('#alerts input[name=fname]').val();
			var lname = $('#alerts input[name=lname]').val();
			var to_fix = [];
			if (!social) to_fix.push('social');
			if (!number) to_fix.push('number');
			if (to_fix.length) {
				_.each(to_fix, function(field) {
					$('input[name=' + field + ']').addClass('fix');
				});
				toastr.error('Please fill in required fields');
			}
			else if (!this.check_social(social)) {
				$('input[name=social]').addClass('fix');
				toastr.error('Social Security Number should be formatted as ###-##-####');
			}
			else if (!this.check_number(number)) {
				$('input[name=number]').addClass('fix');
				toastr.error('Phone Number should be formatted as ###-###-####');
			}
			else {
				console.log(social, number);
				$.ajax({
					method: "POST",
					contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
					url: "/textmessage",
					data: {
						phoneNumber: number,
						social: social,
						fname: fname,
						lname: lname
					}
				}).success(function( msg ){
					console.log("Sent: " + msg);
				});
			}
			return false;
		},

		check_social: function(social) {
			//var social = social.split('-');
			//if (social[0].length != 3) return false;
			//if (social[1].length != 2) return false;
			//if (social[2].length != 4) return false;
			//if (isNaN(social.join(''))) return false;
			return true;
		},

		check_number: function(number) {
			var number = number.split('-');
			if (number[0].length != 3) return false;
			if (number[1].length != 3) return false;
			if (number[2].length != 4) return false;
			if (isNaN(number.join(''))) return false;
			return true;
		}
	});

});