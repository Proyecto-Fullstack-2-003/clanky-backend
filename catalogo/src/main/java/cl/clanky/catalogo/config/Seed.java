package cl.clanky.catalogo.config;

import cl.clanky.catalogo.model.Producto;
import cl.clanky.catalogo.repository.ProductosRepository;
import cl.clanky.catalogo.service.CatalogoService;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

/** Carga el catálogo inicial de Clanky Disquería solo si la tabla está vacía. */
@Configuration
public class Seed {
  // nombre, formato, precio, stock, imagen
  private static final String[][] PRODUCTOS = {
        {"PINK FLOYD - DARK SIDE OF THE MOON", "Vinilo", "34990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/49/86/18/49861852-877b-0992-fa27-58b25fa032b5/196589805232.jpg/316x316bf.webp"},
        {"The Beatles – Abbey Road", "Vinilo", "32990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/df/db/61/dfdb615d-47f8-06e9-9533-b96daccc029f/18UMGIM31076.rgb.jpg/316x316bf.webp"},
        {"Nirvana – Nevermind", "Vinilo", "32990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/95/fd/b9/95fdb9b2-6d2b-92a6-97f2-51c1a6d77f1a/00602527874609.rgb.jpg/316x316bf.webp"},
        {"Arctic Monkeys – AM", "Vinilo", "34990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/69/9c/b5/699cb5d6-115c-ff73-9d26-e57ea4350d72/887828031795.png/316x316bf.webp"},
        {"Radiohead – OK Computer", "Vinilo", "36990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/07/60/ba/0760ba0f-148c-b18f-d0ff-169ee96f3af5/634904078164.png/316x316bf.webp"},
        {"Taylor Swift – Midnights", "Vinilo", "39990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/3d/01/f2/3d01f2e5-5a08-835f-3d30-d031720b2b80/22UM1IM07364.rgb.jpg/316x316bf.webp"},
        {"Billie Eilish – Happier Than Ever", "Vinilo", "39990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/2d/f3/c9/2df3c9fd-e0eb-257c-c035-b04f05a66580/21UMGIM36691.rgb.jpg/316x316bf.webp"},
        {"Lana Del Rey – Born to Die", "Vinilo", "34990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/59/10/66/591066ea-3c85-3dfe-ef82-ffdbbcdfc8b9/12UMGIM00033.rgb.jpg/316x316bf.webp"},
        {"Daft Punk – Random Access Memories", "Vinilo", "49990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/e8/43/5f/e8435ffa-b6b9-b171-40ab-4ff3959ab661/886443919266.jpg/316x316bf.webp"},
        {"The Weeknd – After Hours", "Vinilo", "39990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/6f/bc/e6/6fbce6c4-c38c-72d8-4fd0-66cfff32f679/20UMGIM12176.rgb.jpg/316x316bf.webp"},
        {"Michael Jackson – Thriller", "Vinilo", "29990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/32/4f/fd/324ffda2-9e51-8f6a-0c2d-c6fd2b41ac55/074643811224.jpg/316x316bf.webp"},
        {"Queen – Greatest Hits", "Vinilo", "34990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/b0/ca/05/b0ca05aa-29c9-4e01-5a16-f6fa06bce05c/602527594347.jpg/316x316bf.webp"},
        {"Twenty One Pilots – Blurryface", "Vinilo", "49990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/8e/e2/89/8ee28904-0821-610d-5011-a61845f62756/075679926951.jpg/316x316bf.webp"},
        {"Metallica – Master of Puppets", "Vinilo", "39990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/82/9a/c0/829ac046-9ef9-b027-3257-c2ed6a515707/17UM1IM18688.rgb.jpg/316x316bf.webp"},
        {"Green Day – American Idiot", "Vinilo", "32990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/0e/17/f0/0e17f011-aadf-d4d1-1c7e-b61ce39f968b/093624947301.jpg/316x316bf.webp"},
        {"Coldplay – Parachutes", "Vinilo", "29990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/f5/93/8c/f5938c49-964c-31d1-4b33-78b634f71fb7/190295978075.jpg/316x316bf.webp"},
        {"Bruno Mars & Anderson .Paak – An Evening With Silk Sonic", "Vinilo", "23990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/4e/b2/25/4eb22575-99e3-8c06-7446-51a99add8b0f/075679754585.jpg/316x316bf.webp"},
        {"Olivia Rodrigo – you seem pretty sad for a girl so in love", "Vinilo", "32990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/1d/1b/f9/1d1bf9b1-44c6-9a6c-6ffb-c158488c06ce/26UMGIM39303.rgb.jpg/316x316bf.webp"},
        {"Madonna – The Immaculate Collection", "Vinilo", "48990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Features114/v4/32/fd/6e/32fd6e25-da2b-10e9-df38-2923c01b6c9b/dj.jaxugjvp.jpg/316x316bf.webp"},
        {"Dua Lipa – Dua Lipa", "Vinilo", "18900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/94/fc/6d/94fc6d23-7257-8fe3-fa19-fdcc22e90856/190295559472.jpg/316x316bf.webp"},
        {"Muse – Origin of Symmetry", "Vinilo", "44900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/0c/c8/0b/0cc80b73-aa09-492d-4fcc-984cab29acfa/825646096909.jpg/316x316bf.webp"},
        {"Los Prisioneros – Estadio Nacional Vol. 1", "Vinilo", "53000", "10", "https://i.discogs.com/XlawoFVR7EMlbOiR9TV4rtOiJd55ZQ_76cdQ3YpzZNk/rs:fit/g:sm/q:90/h:460/w:460/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9SLTExMjcw/MzQyLTE1MTMxMTU3/NDQtMjAzOC5qcGVn.jpeg"},
        {"Los Bunkers – La Velocidad de la Luz", "Vinilo", "30000", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/03/ce/46/03ce4676-1cb5-be08-fb5a-d5d019d414f9/192562626160.jpg/316x316bf.webp"},
        {"Chico Trujillo – Mambo Mundial", "Vinilo", "30000", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/c7/ed/86/c7ed8601-9bc0-ca6b-4be1-c597f3688a57/199538043075.jpg/316x316bf.webp"},
        {"My Chemical Romance – The Black Parade", "Vinilo", "39990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/56/99/8a/56998a1c-efe7-fdf0-2b1d-e2da88d8df52/093624917724.jpg/316x316bf.webp"},
        {"My Chemical Romance – Three Cheers for Sweet Revenge", "CD", "39990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/42/58/f7/4258f7bb-eb5b-62b8-573d-6200df0fe3e1/093624917731.jpg/316x316bf.webp"},
        {"Harry Styles – Harry's House", "CD", "12900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/2a/19/fb/2a19fb85-2f70-9e44-f2a9-82abe679b88e/886449990061.jpg/316x316bf.webp"},
        {"Arctic Monkeys – The Car", "CD", "20900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/44/8e/cf/448ecfff-7f7d-286c-8a4a-b631eeeefedb/887828045563.png/316x316bf.webp"},
        {"Mac Miller – The Divine Feminine", "CD", "19990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/33/cc/4d/33cc4d85-e92a-b171-1b72-4dc882ce2359/093624918844.jpg/316x316bf.webp"},
        {"Mac Miller - Circles", "CD", "19990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/23/39/38/23393826-762f-ec76-dc1e-9344f647c958/093624905981.jpg/316x316bf.webp"},
        {"Slipknot – Slipknot", "CD", "12900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/c2/e3/a3/c2e3a370-175c-d67e-7a57-fdb3e1cfc959/016861752507.jpg/316x316bf.webp"},
        {"Air – Moon Safari", "CD", "8900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Features114/v4/ed/cf/a2/edcfa2bc-4a9a-75f6-80b5-6576d826742c/dj.romspvqk.jpg/316x316bf.webp"},
        {"Tyler, The Creator - IGOR", "CD", "13900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/0c/06/05/0c060581-6242-6a2a-a677-20170f2cf8da/886447710180.jpg/316x316bf.webp"},
        {"Billie Eilish - HIT ME HARD AND SOFT", "CD", "18900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/92/9f/69/929f69f1-9977-3a44-d674-11f70c852d1b/24UMGIM36186.rgb.jpg/316x316bf.webp"},
        {"The Weeknd - Starboy", "CD", "12900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/b5/92/bb/b592bb72-52e3-e756-9b26-9f56d08f47ab/16UMGIM67864.rgb.jpg/316x316bf.webp"},
        {"Frank Ocean - Blonde", "CD", "14900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/bb/45/68/bb4568f3-68cd-619d-fbcb-4e179916545d/BlondCover-Final.jpg/316x316bf.webp"},
        {"Kendrick Lamar - DAMN.", "CD", "13990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/86/c9/bb/86c9bb30-fe3d-442e-33c1-c106c4d23705/17UMGIM88776.rgb.jpg/316x316bf.webp"},
        {"Eminem - The Eminem Show", "CD", "9990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music118/v4/dd/5c/e6/dd5ce621-f7d2-f767-7a08-e7a7eaa7870b/00602537526994.rgb.jpg/316x316bf.webp"},
        {"Linkin Park - Hybrid Theory", "CD", "10990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Features115/v4/f0/31/b2/f031b2b2-bcf0-6102-426f-e0b2c7437415/dj.vrgpwamf.jpg/316x316bf.webp"},
        {"The Strokes - Is This It", "CD", "9990", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/cf/18/7b/cf187bcd-029d-5167-bab6-87219f32d95a/078636804521.jpg/316x316bf.webp"},
        {"The Smiths - The Queen Is Dead", "CD", "10900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/1a/e8/70/1ae870c3-b402-096b-c4c4-8022af5a2ed9/745099189662.jpg/316x316bf.webp"},
        {"Olivia Rodrigo - SOUR", "CD", "15900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/ce/63/06/ce6306bb-5830-af8f-8ebd-4eb7d3c14e1e/21UMGIM26092.rgb.jpg/316x316bf.webp"},
        {"Twenty One Pilots - Trench", "CD", "20900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/a3/c0/2b/a3c02b76-baa1-e575-dcba-247509200424/075679864789.jpg/316x316bf.webp"},
        {"Paramore - This Is Why", "CD", "19900", "10", "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/21/de/99/21de99a3-6eb6-5372-6a96-40d992808d9c/075679716224.jpg/316x316bf.webp"},
        {"Paramore - Paramore", "CD", "10900", "10", "https://dojiw2m9tvv09.cloudfront.net/41657/product/X_21245907466.jpg?114&t=1788215400"},
        {"Puffle Celeste", "Merch", "5900", "10", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ7rlbsRFED5Td0dJ88hIIsNLEbQsYyuMNSEsTk06yeOQ&s=10"},
        {"Puffle Rojo", "Merch", "5900", "10", "https://static.wikia.nocookie.net/iclubpenguinofficial/images/8/8c/Red_Puffle.png/revision/latest?cb=20180215192259"},
        {"Puffle Verde", "Merch", "5900", "10", "https://static.wikia.nocookie.net/iclubpenguinofficial/images/1/1b/Green_Puffle.png/revision/latest?cb=20180215192955"},
        {"Puffle Amarillo", "Merch", "5900", "10", "https://static.wikia.nocookie.net/iclubpenguinofficial/images/9/97/Yellow_Puffle.png/revision/latest/scale-to-width-down/1000?cb=20180212230559"},
        {"Puffle Morado", "Merch", "5900", "10", "https://static.wikia.nocookie.net/iclubpenguinofficial/images/7/75/Purple_Puffle.png/revision/latest/scale-to-width-down/1000?cb=20180215192943"},
        {"Polera The Cure Bloodflowers", "Merch", "26900", "10", "https://cdn.media.amplience.net/s/hottopic/36926693_hi?$productMainDesktop$&fmt=auto"},
        {"Polera Blink-182 Daisy Face Logo", "Merch", "24500", "5", "https://cdn.media.amplience.net/s/hottopic/37029982_hi?$productMainDesktop$&fmt=auto"},
        {"Polera Twenty One Pilots More Than Imagined", "Merch", "25900", "15", "https://cdn.media.amplience.net/s/hottopic/36598943_hi?$productMainDesktop$&fmt=auto"},
        {"Polera Linkin Park Hybrid Theory Symbols", "Merch", "24900", "10", "https://cdn.media.amplience.net/s/hottopic/34598444_hi?$productMainDesktop$&fmt=auto"},
        {"Billie Eilish Hit Me Hard And Soft Guitar Portrait", "Merch", "34900", "10", "https://cdn.media.amplience.net/s/hottopic/36337394_hi?$productMainDesktop$&fmt=auto"}
  };

  @Bean
  CommandLineRunner cargarDatos(ProductosRepository repo) {
    return args -> {
      if (repo.count() == 0) {
        for (String[] d : PRODUCTOS) {
          Producto p = new Producto();
          p.nombre = d[0];
          p.nombreClave = CatalogoService.normalizar(d[0]);
          p.formato = d[1];
          p.precio = new BigDecimal(d[2]);
          p.stock = Integer.parseInt(d[3]);
          p.imagen = d[4];
          repo.save(p);
        }
      }
    };
  }
}
