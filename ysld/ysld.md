# Ysld

The following is an outline of the Ysld language:

    #
    # common definitions, not actually part of the syntax
    #
    define:
      graphic: &graphic
        symbols:
        - mark:
            shape: <shape>
            <<: *fill
            <<: *stroke
        - external:
            url: <text>
            format: <text>
        anchor: <tuple>
        displacement: <tuple>
        opacity: <expression>
        rotation: <expression>
        size: <expression>
        options: <options>
        gap: <expression>
        initial-gap: <expression>

      fill: &fill
        fill-color: <color>
        fill-opacity: <expression>
        fill-graphic: 
          <<: *graphic

      stroke: &stroke 
        stroke-color: <color>
        stroke-width: <expression>
        stroke-opacity: <expression>
        stroke-linejoin: <expression>
        stroke-linecap: <expression>
        stroke-dasharray: <float[]>
        stroke-dashoffset: <expression>
        stroke-graphic-fill: 
          <<: *graphic
        stroke-graphic-stroke: 
          <<: *graphic

      symbolizer: &symbolizer
        geometry: <expression>
        <options>

    #
    # start of syntax
    #
    name: <text>
    title: <text>
    abstract: <text>
    feature-styles:
    - name: <text>
      title: <text>
      abstract: <text>
      transform:
        name: <text>
        params: <args>
      rules:
      - name: <text>
        title: <text>
        abstract: <text>
        scale: <tuple>
        filter: <filter>
        else: <bool>
        symbolizers:
        - point:
            <<: *graphic
            <<: *symbolizer
        - line: 
            <<: *stroke
            offset: <expression>
            <<: *symbolizer
        - polygon:
            <<: *fill
            <<: *stroke
            offset: <expression>
            displacement: <tuple>
            <<: *symbolizer
        - raster: 
            color-map: 
              type: ramp|intervals|values
              entries:
              - <quad> # color, opacity, value, label
            opacity: <expression>
            contrast-enhancement: 
              mode: normalize|histogram|none
              gamma: <expression>
            <<: *symbolizer
        - text:
            label: <expression>
            font-family: <expression>
            font-size: <expression>
            font-style: <expression>
            font-weight: <expression>
            placement:
              type: point|line
              offset: <expression>
              anchor: <tuple>
              displacement: <tuple>
              rotation: <expression>
            <<: *fill
            <<: *symbolizer
            <<: *graphic

<a name="expression"></a>

## Colors

Colors literals can be specified either as a 6 digit hex string or a 3 argument 
rgb function call. Examples:

    stroke-color: #ff00ff
    fill-color: rgb(255,0,255)

## Expressions

Expressions are specified as CQL/ECQL parse-able expression strings. See the 
[cql_docs] and this [cql_tutorial] for more information about the CQL syntax. 

[cql_docs]: http://docs.geotools.org/stable/userguide/library/cql/ecql.html "CQL documentation"
[cql_tutorial]: http://docs.geoserver.org/latest/en/user/tutorials/cql/cql_tutorial.html "CQL tutorial"

The following are some simple examples:

### Literals

    stroke-width: 10
    stroke-linecap: 'butt'

Note: Single quotes are needed for string literals to differentiate them from
attribute references. 

### Attributes

    text:
      label: [STATE_NAME]

### Functions

    point:
      rotation: sqrt([STATE_POP])

## Filters

Rule filters are specified as CQL/ECQL parse-able filters. A simple example:

    rules:
    - filter: [type] = 'highway'
      symbolizers:
      - line:
          stroke-width: 5

See the [cql_docs] and this [cql_tutorial] for more information about the CQL 
syntax. 

## Tuples

Some attributes are specified as pairs. For example:

    rules:
    - scale: (10000,20000)

    point:
      anchor: (0.5,0.5)

One of the values in the tuple may be omitted as in:

    rules:
    - scale: (,10000)
    - scale: (10000,)

## Hints

Symbolizer hints are specified as normal mappings on a symbolizer object. Hints start with the prefix 'x-' and are limited to numeric, bool and text (no expressions).

If you are checking the GeoServer docs hints are called "vendor options":

* [user manual](http://docs.geoserver.org/latest/en/user/styling/sld-reference/labeling.html)
* [style workshop](https://github.com/boundlessgeo/workshops/tree/master/workshops/geoserver/style/source/style)
* [javadocs](http://docs.geotools.org/stable/javadocs/org/geotools/styling/TextSymbolizer.html)

Hints can be used with any symbolizer:

    point:
      ...
      # No labels should overlap this feature, used to ensure point graphics are clearly visible
      # and not obscured by text
      x-labelObstacle: true

The majority of hints focus on controlling text:

    text:
      # When false does not allow labels on lines to get beyond the beginning/end of the line. 
      # By default a partial overrun is tolerated, set to false to disallow it.
      x-allowOverruns: false
      
      # Number of pixels are which a long label should be split into multiple lines. Works on all
      # geometries, on lines it is mutually exclusive with the followLine option
      x-autoWrap: true
      
      # Enables conflict resolution (default, true) meaning no two labels will be allowed to
      # overlap. Symbolizers with conflict resolution off are considered outside of the
      # conflict resolution game, they don't reserve area and can overlap with other labels.
      x-conflictResolution: true
      
      # When true activates curved labels on linear geometries. The label will follow the shape of 
      # the current line, as opposed to being drawn a tangent straight line
      x-followLine: true
      
      # When true forces labels to a readable orientation, when false they make follow the line
      # orientation even if that means the label will look upside down (useful when using
      # TTF symbol fonts to add direction markers along a line)
      x-forceLeftToRight: true
      
      # Sets the percentage of the label that must sit inside the geometry to allow drawing
      # the label. Works only on polygons.
      x-goodnessOfFit: 90
      
      # Pixels between the stretched graphic and the text, applies when graphic stretching is in use
      x-graphic-margin: 10
      
      # Stretches the graphic below a label to fit the label size. Possible values are 'stretch',
      # 'proportional'.
      x-graphic-resize: true

      # If true, geometries with the same labels are grouped and considered a single entity to be
      # abeled. This allows to avoid or control repeated labels
      x-group: 'zone'

      # When false,  only the biggest geometry in a group is labelled (the biggest is obtained by
      # merging, when possible, the original geometries). When true, also the smaller items in the
      # group are labeled. Works only on lines at the moment.
      x-labelAllGroup: false
      
      # When positive it's the desired distance between two subsequent labels on a "big" geometry.
      # Works only on lines at the moment. If zero only one label is drawn no matter how big the
      # geometry is
      x-repeat: 0

      # When drawing curved labels, max allowed angle between two subsequent characters. Higher
      # angles may cause disconnected words or overlapping characters
      x-maxAngleDelta: 90

      # The distance, in pixel, a label can be displaced from its natural position in an attempt to
      # find a position that does not conflict with already drawn labels.
      x-maxDisplacement: 400
      
      # Minimum distance between two labels in the same label group. To be used when both
      # displacement and repeat are used to avoid having two labels too close to each other
      x-minGroupDistance: 3

      # Option to truncate labels placed on the border of the displayArea (display partial labels).
      x-partials: true
      
      # Option overriding manual rotation to align label rotation automatically for polygons.
      x-polygonAlign: true
      
      # The minimum distance between two labels, in pixels
      x-spaceAround: 50

## Arrays

Lists and arrays are specified as space delimited. For example:

    stroke-dasharray: 5 2 1 2

## Anchors & References

With Yaml it is possible to reference other parts of a document. With this 
it is possible to support variables and mix ins. An example of a color variable:

    redish: &redish #DD0000
    point:
      fill-color: *redish

An named "anchor" is declared with the `&` character and then referenced with 
the `*` character. This same feature can be used to do "mix-ins" as well:

    define: &highway_zoom10
      scale: (10000,20000)
      filter: type = 'highway'

    rules:
    - >>: *highway_zoom10
      symbolizers:
      - point

The syntax in this case is slightly different and is used when referencing an 
entire mapping object rather than just a simple scalar value. 
