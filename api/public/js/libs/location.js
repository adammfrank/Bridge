$(function(){
    var user_point,
        user_court,
        court_point,
        last_user_court,
        user_coords,
        search_count = 0;
    var selected_court = {};
    var shape = [];
    var state = document.getElementById('state');
    var court_info = document.getElementById('court-info');
    var map = L.map('map', {
        center: [38.635, -90.251],
        zoom: 10,
        scrollWheelZoom: true,
        doubleClickZoom: false
    });

    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Options for GeoLocator
    var options = {
        collapsed: true, /* Whether its collapsed or not */
        position: 'topright', /* The position of the control */
        text: 'Search', /* The text of the submit button */
        bounds: null, /* a L.LatLngBounds object to limit the results to */
        email: null, /* an email string with a contact to provide to Nominatim. Useful if you are doing lots of queries */
        callback: function (results) {
            console.log(results);
            console.log(results[0].lat);
            addMarker([parseFloat(results[0].lat), parseFloat(results[0].lon)]);
            search_count++;
            //var bbox = results[0].boundingbox,
            //    first = new L.LatLng(bbox[0], bbox[2]),
            //    second = new L.LatLng(bbox[1], bbox[3]),
            //    bounds = new L.LatLngBounds([first, second]);
            //this._map.fitBounds(bounds);
        }
    };
    var osmGeocoder = new L.Control.OSMGeocoder(options);

    map.addControl(osmGeocoder);

    var court_boundary = new L.geoJson();
    //court_boundary.addTo(map);

    $.ajax({
        dataType: "json",
        url: "data/courts.json",
        success: function(data) {
            $(data.features).each(function(key, data) {
                court_boundary.addData(data);
                //L.marker([data.properties.lat, data.properties.lng]).addTo(map).bindPopup(data.properties.court_name);
                //console.log(data.properties.lat);
                //console.log(court_boundary);
            });
        }
    }).error(function() {});

    /*
     * Add marker upon user click
     */

    map.on('click', addMarker);

    $('.locate').on('click', function(){
        map.locate();
    });

    map.on('locationfound', onLocationFound);

    function onLocationFound(e) {
        console.log(e);
        addMarker([e.latitude, e.longitude]);
    }
    function addMarker(point){

        if(user_point){
            map.removeLayer(user_point);
            map.removeLayer(court_point);
            map.removeLayer(user_court);
        }
        if(point.containerPoint){
            user_coords = point.latlng;

        }
        else{

            user_coords = L.latLng(point[0], point[1]);
        }
        console.log(user_coords);
        var layer = leafletPip.pointInLayer(user_coords, court_boundary, true);
        console.log(layer);
        if (layer.length) {
            console.log(layer[0]);
            state.innerHTML = '<strong>' + layer[0].feature.properties.court_name + '</strong>';
            selected_court = layer[0].feature.properties;
            console.log(selected_court);
            court_info.innerHTML ='<ul><li>Court Name: ' +  selected_court.court_name + "</li>"
                                 +'<li>Court ID: ' +  selected_court.court_id + "</li>"
                                 +'<li>Address: ' +  selected_court.addr_1 + "</li>"
                                 +'<li>Hours: ' +  selected_court.hours + "</li>"
                                 +'<li>Phone Number: ' +  selected_court.phone_number + "</li></ul>";

            shape = layer[0].feature.geometry.coordinates[0];
            for(var i = 0; i < shape.length; i++){
                var district = shape[i];
                var temp_point = district[0];
                if(temp_point < 0) {
                    district[0] = district[1];
                    district[1] = temp_point;
                }
            }
            console.log("");

            user_court = L.polygon(shape).addTo(map);
            console.log(user_court._latlngs[0].lat);
            map.fitBounds(user_court.getBounds());
            last_user_court = user_court._latlngs[0].lat;
            court_point = new L.marker([layer[0].feature.properties.lat, layer[0].feature.properties.lng]).addTo(map)
                .bindPopup("<h3>" + layer[0].feature.properties.court_name + " Courthouse</h3>").openPopup();
            //console.log(layer[0].feature.geometry.coordinates[0]);
        } else {
            state.innerHTML = '';
        }

        user_point = new L.marker(user_coords).addTo(map).bindPopup("You");
    }
});