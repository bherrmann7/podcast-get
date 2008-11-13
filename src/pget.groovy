/*
I use this script to download new mp3's into a directory.   I then copy the files onto my mp3 player and
listen to them during my commute.  When I'm need a recharge of new mp3s, I re-run the script to get more podcasts.

 How to get and use this script
   1. Download this script as pget.groovy (click the 'raw' link on this github page.)  --------------------^^^  
   2. Create a directory to save the podcast in (for example, mkdir /tmp/podcasts )
   3. Edit pget.groovy and change the sites to be url's to podcasts you like
   4. Run like this, "groovy pget.groovy <downloadLocation>" (ie. groovy pget.groovy /tmp/podcasts)
 Enjoy.

This script;
- numbers the files uniquely (so there arent any naming collisions)
- preserves the download order so I hear my most important podcasts first.
- keeps track of what is downloaded (so you don't get the same podcasts over and over.)
- only downloads a max of 3 podcasts from each source (so when first using this script you don't get 100 podcasts)
- You can stop the script and edit and re-run it (tweaker friendly). Rerunning is safe because it doesn't update the history until the end and it skips already downloaded files.

*/
// groovy 1.6beta2 has a bug. uncomment this line to use it.
// def args = [ "/tmp/podcasts" ]
if (args.size() != 1 ){
   println "usage: pget downloadDirectory"
   System.exit(1)
}
def downloadLocation = args[0]
if (  !new File(downloadLocation).isDirectory() ){
    println "Error: The argument (${downloadLocation}) is not a directory."
    System.exit(-1);
}
downloadLocation += File.separator + "%04d-%s"

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
                println "Will get: " + filename + " " + url
                enclosures[url] = filename
            }

        }
    }
}

println "\nstarting downloads... will get " + enclosures.size() + " files"

errors = 0;

enclosures.each {url, filename ->
    println "downloading: " + filename + " " + url
    ondiskname = String.format(downloadLocation, downloadHistory.size(), filename)

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
