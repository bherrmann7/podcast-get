

def downloadHistory = []
def downloadHistoryFile = new File("downloadHistory.groovy");
if ( downloadHistoryFile.exists() ){
   downloadHistory = evaluate(downloadHistoryFile.text)
} else {
   println "Warning: No download history found, creating a new one."
}

println "running, cached: " + downloadHistory.size()

sites = [
        "http://hansamann.podspot.de/rss",
        "http://www.pbs.org/cringely/pulpit/rss/podcast.rss.xml",
        "http://leoville.tv/podcasts/ww.xml",
        "http://www.nofluffjuststuff.com/s/podcast/itunes.xml",
        "http://media.ajaxian.com/",
        "http://feeds.feedburner.com/javaposse",
        "http://www.discovery.com/radio/xml/sciencechannel.xml",
        "http://feeds.feedburner.com/gigavox/channel/itconversations",
        "http://feeds.feedburner.com/rubyonrailspodcast",
        "http://www.slate.com/podcast/",
        "http://www.scienceandsociety.net/podcasts/index.xml",
        "http://blog.stackoverflow.com/index.php?feed=podcast",
        "http://leoville.tv/podcasts/floss.xml",
        "http://leoville.tv/podcasts/dgw.xml",
        "http://leoville.tv/podcasts/jm.xml"

]
enclosures = [:]

histmap = [:]
downloadHistory.each {urlName, fileName ->
    histmap[urlName] = fileName
}

sites.each {site ->
//site = sites[0]
    println "site: ${site}"
    xml = new groovy.util.XmlSlurper().parse(site)

    int max = 3;

    def xmlenclosures = xml.depthFirst().findAll { it.name().equals("enclosure") }
    println "   has ${xmlenclosures.size()} enclosures"
    xmlenclosures.each {enclosure ->
        //println "enclosure " + enclosure.@url + " or " + enclosure.@url.toString()

        url = enclosure.@url.toString()
        filename = url.toString().substring(url.toString().lastIndexOf('/') + 1);
        //println "$filename  of  $url"

        if (max-- > 0) {
            if (histmap[filename]) {
                println "already have $filename"
                // prevend downloading past 'areadly have'
                max = 0;
            } else {
                println "X-X Will get: " + filename + " " + url
                enclosures[url] = filename
            }

        }
    }
}

println "\nstarting downloads... will get " + enclosures.size() + " files"

errors = 0;

enclosures.each {url, filename ->
    println "downloading: " + filename + " " + url
    ondiskname = String.format("c:/pcast/%04d-%s", downloadHistory.size(), filename)
    ondiskname = String.format("/home/bob/pcast/%04d-%s", downloadHistory.size(), filename)

    file = new File(ondiskname)
    file.parentFile.mkdir()

    if (file.exists()) {
        println "**** ALREADY DOWNLOADED: ${url}"
    } else {
        def fileOut = new FileOutputStream(file)
        def out = new BufferedOutputStream(fileOut)
        out << new URL(url).openStream()
        out.close()
    }
    println "ok have downloadHistory[$filename]=$ondiskname"

    odnf = new File(ondiskname);
    downloadHistory << [filename, odnf.name]
}

// saved new items
downloadHistoryFile.withPrintWriter {pw ->
    pw.println("def history = [ ")
    downloadHistory.each {
        pw.println " [ '${it[0]}', '${it[1]}' ],"
    }
    pw.println("]")
}

println "all done, cache:" + downloadHistory.size() + ", added:" + enclosures.size() + ", errors:" + errors
