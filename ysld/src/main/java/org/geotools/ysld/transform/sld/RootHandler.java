package org.geotools.ysld.transform.sld;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

public class RootHandler extends SldTransformHandler {
    @Override
    public void document(XMLStreamReader xml, SldTransformContext context)
            throws XMLStreamException, IOException {
        super.document(xml, context);
        context.document();
    }

    @Override
    public void element(XMLStreamReader xml, SldTransformContext context) throws XMLStreamException, IOException {
        super.element(xml, context);
        if ("UserStyle".equals(xml.getName().getLocalPart())) {
            context.mapping().push(new UserStyleHandler());
        }
    }

    @Override
    public void endDocument(XMLStreamReader xml, SldTransformContext context) throws XMLStreamException, IOException {
        super.endDocument(xml, context);
        context.endDocument();
    }

    static class UserStyleHandler extends SldTransformHandler {

        @Override
        public void element(XMLStreamReader xml, SldTransformContext context) throws XMLStreamException, IOException {
            String name = xml.getLocalName();
            if ("Name".equals(name)) {
                context.scalar("name");
                context.scalar(xml.getElementText());
            }
            else if ("Title".equals(name)) {
                context.scalar("title");
                context.scalar(xml.getElementText());
            }
            else if ("Abstract".equals(name)) {
                context.scalar("abstract");
                context.scalar(xml.getElementText());
            }
            else if ("FeatureTypeStyle".equals(name)) {
                context.scalar("feature-styles").sequence().push(new FeatureStylesHandler());
            }
        }

        @Override
        public void endElement(XMLStreamReader xml, SldTransformContext context) throws XMLStreamException, IOException {
            if ("UserStyle".equalsIgnoreCase(xml.getLocalName())) {
                context.endSequence().endMapping().pop();
            }
        }
    }
}
